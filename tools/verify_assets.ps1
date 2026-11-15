# Verifies asset cross-references for the Ultima Elevators mod (regex-based; PS 5.1 compatible):
#   blockstate -> model -> texture chains, parent models, loot/recipe ids, lang keys
$ErrorActionPreference = 'Stop'
$assets = Join-Path $PSScriptRoot '..\src\main\resources\assets\ultimaelevators'
if (-not (Test-Path $assets)) { Write-Error "assets dir not found: $assets" }

$errors = [System.Collections.Generic.List[string]]::new()

function Get-Ns([string]$ref) {
    if ($ref -match '^([^:]+):(.+)$') { return @{ ns = $Matches[1]; path = $Matches[2] } }
    return @{ ns = 'minecraft'; path = $ref }
}
function Test-ModelRef([string]$ref, [string]$what) {
    if ([string]::IsNullOrWhiteSpace($ref)) { $errors.Add("$what : empty model ref"); return }
    $n = Get-Ns $ref
    if ($n.ns -eq 'ultimaelevators') {
        if (-not (Test-Path (Join-Path $assets ("models\" + $n.path + ".json")))) {
            $errors.Add("$what : missing model '$ref'")
        }
    } elseif ($n.ns -ne 'minecraft') {
        $errors.Add("$what : unknown namespace '$ref'")
    }
}
function Test-TextureRef([string]$ref, [string]$what) {
    if ($ref -like '#*') { return }   # resolved by caller
    $n = Get-Ns $ref
    if ($n.ns -ne 'ultimaelevators') { return }
    # texture refs already include their category (block/, item/, entity/...): resolve from textures root
    if (Test-Path (Join-Path $assets ("textures\" + $n.path + ".png"))) { return }
    $errors.Add("$what : missing texture '$ref'")
}

# 1. blockstates -> models
$bsFiles = Get-ChildItem (Join-Path $assets 'blockstates') -Filter *.json
foreach ($f in $bsFiles) {
    $txt = Get-Content $f.FullName -Raw
    $ms = [regex]::Matches($txt, '"model"\s*:\s*"([^"]+)"')
    foreach ($m in $ms) { Test-ModelRef $m.Groups[1].Value ("blockstate " + $f.Name) }
}

# 2. models -> parents and textures
$modelFiles = @(Get-ChildItem (Join-Path $assets 'models\block') -Filter *.json -Recurse) +
              @(Get-ChildItem (Join-Path $assets 'models\item') -Filter *.json -Recurse)
foreach ($f in $modelFiles) {
    $txt = Get-Content $f.FullName -Raw
    $what = "model " + $f.Name
    if ($txt -match '"parent"\s*:\s*"([^"]+)"') { Test-ModelRef $Matches[1] $what }
    # extract the flat "textures" object
    if ($txt -match '"textures"\s*\:\s*\{([^{}]*)\}') {
        $block = $Matches[1]
        $defs = [regex]::Matches($block, '"([^"]+)"\s*:\s*"([^"]*)"')
        $names = @{}
        foreach ($d in $defs) { $names[$d.Groups[1].Value] = $d.Groups[2].Value }
        foreach ($k in $names.Keys) {
            $v = $names[$k]
            if ($v -like '#*') {
                $refName = $v.Substring(1)
                if (-not $names.ContainsKey($refName)) {
                    $errors.Add("$what : texture '$k' references undefined '$v'")
                }
            } else {
                Test-TextureRef $v $what
            }
        }
    } elseif (-not ($txt -match '"parent"\s*:\s*')) {
        # models that parent to another model inherit its textures
        $errors.Add("$what : no textures object and no parent")
    }
}

# 3. entity textures referenced by java code
foreach ($t in @('cab','door')) {
    if (-not (Test-Path (Join-Path $assets "textures\entity\elevator\$t.png"))) {
        $errors.Add("missing entity texture entity/elevator/$t.png")
    }
}

# 4. loot tables for every blockstate (block ids)
$dataRoot = Join-Path $PSScriptRoot '..\src\main\resources\data\ultimaelevators'
foreach ($f in $bsFiles) {
    $id = [IO.Path]::GetFileNameWithoutExtension($f.Name)
    # loot tables live under data/<ns>/loot_tables/blocks (assets copy is ignored by the game)
    $lt = Join-Path $dataRoot "loot_tables\blocks\$id.json"
    if (-not (Test-Path $lt)) { $errors.Add("missing loot table for block '$id'") }
}

# 5. recipes reference known ids (blockstates + item models + java registry names)
$knownIds = @{}
foreach ($f in $bsFiles) { $knownIds[[IO.Path]::GetFileNameWithoutExtension($f.Name)] = $true }
foreach ($f in $modelFiles) {
    if ($f.Directory.Name -eq 'item') { $knownIds[[IO.Path]::GetFileNameWithoutExtension($f.Name)] = $true }
}
$javaSrc = Join-Path $PSScriptRoot '..\src\main\java'
foreach ($jf in (Get-ChildItem $javaSrc -Recurse -Filter *.java)) {
    $txt = Get-Content $jf.FullName -Raw
    foreach ($m in [regex]::Matches($txt, '"([A-Z0-9_]+)"\s*\)')) { $knownIds[$m.Groups[1].Value.ToLower()] = $true }
}
foreach ($f in (Get-ChildItem (Join-Path $dataRoot 'recipes') -Filter *.json)) {
    $txt = Get-Content $f.FullName -Raw
    $what = "recipe " + $f.Name
    foreach ($m in [regex]::Matches($txt, '"ultimaelevators:([a-z0-9_/]+)"')) {
        $id = $m.Groups[1].Value
        if (-not $knownIds.ContainsKey($id)) { $errors.Add("$what : unknown mod id 'ultimaelevators:$id'") }
    }
}

# 6. lang keys used in java exist in en_us.json and tr_tr.json
$langFiles = @{}
foreach ($name in @('en_us','tr_tr')) {
    $lf = Join-Path $assets "lang\$name.json"
    $txt = Get-Content $lf -Raw
    $keys = @{}
    foreach ($m in [regex]::Matches($txt, '"((?:[a-z_]+\.)+[a-z0-9_.]+)"\s*:')) { $keys[$m.Groups[1].Value] = $true }
    $langFiles[$name] = $keys
}
$used = [System.Collections.Generic.List[string]]::new()
foreach ($jf in (Get-ChildItem $javaSrc -Recurse -Filter *.java)) {
    $txt = Get-Content $jf.FullName -Raw
    foreach ($m in [regex]::Matches($txt, '"((?:status|screen|tooltip|message|key|itemGroup|entity|block)\.ultimaelevators\.[^"]*)"')) {
        $used.Add($m.Groups[1].Value)
    }
}
foreach ($k in ($used | Select-Object -Unique)) {
    if ($k.EndsWith('.')) { continue }   # string-concat prefix, e.g. "status.ultimaelevators." + key
    foreach ($name in $langFiles.Keys) {
        if (-not $langFiles[$name].ContainsKey($k)) {
            $errors.Add("lang: '$k' used in java but missing in $name.json")
        }
    }
}

if ($errors.Count -eq 0) {
    Write-Host 'OK - all asset cross-references verified'
    Write-Host ("blockstates: {0}, models: {1}, recipes: {2}" -f $bsFiles.Count, $modelFiles.Count,
                (Get-ChildItem (Join-Path $dataRoot 'recipes') -Filter *.json).Count)
} else {
    Write-Host ("FAIL - {0} problems:" -f $errors.Count)
    $errors | ForEach-Object { Write-Host "  $_" }
}
