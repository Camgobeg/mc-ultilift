# Generates all Ultima Elevators textures programmatically.
# Run: powershell -ExecutionPolicy Bypass -File tools/make_textures.ps1
Add-Type -AssemblyName System.Drawing

$root = Join-Path $PSScriptRoot "..\src\main\resources\assets\ultimaelevators\textures"
$blockDir = Join-Path $root "block"
$itemDir  = Join-Path $root "item"
$entDir   = Join-Path $root "entity\elevator"
foreach ($d in @($blockDir, $itemDir, $entDir)) {
    New-Item -ItemType Directory -Force -Path $d | Out-Null
}

function Clamp([int]$v) { [Math]::Max(0, [Math]::Min(255, $v)) }

function New-Tex([int]$w, [int]$h) { New-Object System.Drawing.Bitmap($w, $h) }

function Fill-Noise([System.Drawing.Bitmap]$bmp, [int]$r, [int]$g, [int]$b, [int]$var, [int]$seed, [int]$a = 255) {
    $rnd = New-Object System.Random($seed)
    for ($y = 0; $y -lt $bmp.Height; $y++) {
        for ($x = 0; $x -lt $bmp.Width; $x++) {
            $d = $rnd.Next(-$var, $var + 1)
            $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($a, (Clamp ($r + $d)), (Clamp ($g + $d)), (Clamp ($b + $d))))
        }
    }
}

function Rect([System.Drawing.Bitmap]$bmp, [int]$x1, [int]$y1, [int]$x2, [int]$y2, [int]$r, [int]$g, [int]$b, [int]$a = 255) {
    for ($y = $y1; $y -le $y2; $y++) {
        for ($x = $x1; $x -le $x2; $x++) {
            if ($x -ge 0 -and $y -ge 0 -and $x -lt $bmp.Width -and $y -lt $bmp.Height) {
                $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($a, (Clamp $r), (Clamp $g), (Clamp $b)))
            }
        }
    }
}

function Outline([System.Drawing.Bitmap]$bmp, [int]$t, [int]$r, [int]$g, [int]$b, [int]$a = 255) {
    $w = $bmp.Width; $h = $bmp.Height
    Rect $bmp 0 0 ($w - 1) ($t - 1) $r $g $b $a
    Rect $bmp 0 ($h - $t) ($w - 1) ($h - 1) $r $g $b $a
    Rect $bmp 0 0 ($t - 1) ($h - 1) $r $g $b $a
    Rect $bmp ($w - $t) 0 ($w - 1) ($h - 1) $r $g $b $a
}

function VLine([System.Drawing.Bitmap]$bmp, [int]$x, [int]$r, [int]$g, [int]$b, [int]$a = 255) {
    Rect $bmp $x 0 $x ($bmp.Height - 1) $r $g $b $a
}

function HLine([System.Drawing.Bitmap]$bmp, [int]$y, [int]$r, [int]$g, [int]$b, [int]$a = 255) {
    Rect $bmp 0 $y ($bmp.Width - 1) $y $r $g $b $a
}

function Circle([System.Drawing.Bitmap]$bmp, [double]$cx, [double]$cy, [double]$rad, [int]$r, [int]$g, [int]$b, [int]$a = 255) {
    for ($y = 0; $y -lt $bmp.Height; $y++) {
        for ($x = 0; $x -lt $bmp.Width; $x++) {
            $dx = $x - $cx; $dy = $y - $cy
            $dist = [Math]::Sqrt($dx * $dx + $dy * $dy)
            if ($dist -le $rad) {
                $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($a, (Clamp $r), (Clamp $g), (Clamp $b)))
            }
        }
    }
}

function BrushedMetal([System.Drawing.Bitmap]$bmp, [int]$r, [int]$g, [int]$b, [int]$seed) {
    Fill-Noise $bmp $r $g $b 14 $seed
    # horizontal brushed streaks
    $rnd = New-Object System.Random($seed + 1)
    for ($i = 0; $i -lt 10; $i++) {
        $y = $rnd.Next(0, $bmp.Height)
        $d = (Get-Random -Minimum -10 -Maximum 11)
        HLine $bmp $y (Clamp ($r + $d)) (Clamp ($g + $d)) (Clamp ($b + $d)) 255
    }
}

function Save([System.Drawing.Bitmap]$bmp, [string]$path) {
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

function Px([System.Drawing.Bitmap]$bmp, [int]$x, [int]$y, [int]$r, [int]$g, [int]$b, [int]$a = 255) {
    if ($x -ge 0 -and $y -ge 0 -and $x -lt $bmp.Width -and $y -lt $bmp.Height) {
        $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($a, (Clamp $r), (Clamp $g), (Clamp $b)))
    }
}

# ---------------- block textures ----------------

# shaft_frame: steel lattice (X pattern)
$t = New-Tex 16 16
BrushedMetal $t 108 112 118 11
Outline $t 2 64 66 70
for ($i = 0; $i -lt 16; $i++) { Px $t $i $i 70 72 78; Px $t (15 - $i) $i 70 72 78 }
for ($i = 2; $i -lt 14; $i++) { Px $t $i (15 - $i) 148 152 158; Px $t (15 - $i) (15 - $i) 148 152 158 }
Save $t (Join-Path $blockDir "shaft_frame.png")

# shaft_glass: translucent pane with steel border
$t = New-Tex 16 16
Fill-Noise $t 150 190 210 12 22 90
Outline $t 2 96 104 112
VLine $t 4 120 130 140 140
VLine $t 11 120 130 140 140
Rect $t 5 8 10 9 220 240 250 120
Save $t (Join-Path $blockDir "shaft_glass.png")

# shaft_rail: vertical guide rail
$t = New-Tex 16 16
BrushedMetal $t 124 128 134 33
Outline $t 1 70 72 78
Rect $t 6 0 9 15 96 100 108
Rect $t 7 0 8 15 150 154 160
VLine $t 7 170 174 180
VLine $t 8 170 174 180
Save $t (Join-Path $blockDir "shaft_rail.png")

# cw_rail: counterweight rail with weight plates
$t = New-Tex 16 16
BrushedMetal $t 96 100 106 44
Outline $t 1 52 54 60
Rect $t 3 2 12 13 70 74 80
Rect $t 4 3 11 12 120 124 130
HLine $t 5 170 174 180
HLine $t 10 170 174 180
VLine $t 7 150 154 160
VLine $t 8 150 154 160
Save $t (Join-Path $blockDir "cw_rail.png")

# oil_buffer: black base + oil sheen
$t = New-Tex 16 16
Fill-Noise $t 30 32 36 8 55
Outline $t 2 90 94 100
Circle $t 5.5 5.5 2.4 70 80 95
Circle $t 10.5 10.5 2.2 60 70 85
Circle $t 6.5 12.0 1.6 85 95 110
Px $t 5 4 140 150 170
Px $t 10 9 140 150 170
Save $t (Join-Path $blockDir "oil_buffer.png")

# spring_buffer: coil
$t = New-Tex 16 16
BrushedMetal $t 104 108 114 66
Outline $t 2 70 72 78
for ($y = 2; $y -le 13; $y++) {
    $x = [int][Math]::Round(7.5 + 4.5 * [Math]::Sin($y * 1.9))
    Px $t $x $y 190 196 202
    Px $t ($x + 1) $y 120 124 130
}
Rect $t 0 2 15 3 80 84 90
Rect $t 0 12 15 13 80 84 90
Save $t (Join-Path $blockDir "spring_buffer.png")

# controller_front
$t = New-Tex 16 16
BrushedMetal $t 120 124 130 77
Outline $t 1 80 84 90
Rect $t 3 3 12 6 20 26 34
Rect $t 4 4 11 5 40 190 220
Rect $t 3 9 5 11 30 32 38
Rect $t 7 9 9 11 200 60 60
Rect $t 11 9 13 11 60 200 90
HLine $t 13 90 94 100
Save $t (Join-Path $blockDir "controller_front.png")

# controller_side / top
$t = New-Tex 16 16
BrushedMetal $t 116 120 126 88
Outline $t 1 84 88 94
Save $t (Join-Path $blockDir "controller_side.png")

$t = New-Tex 16 16
BrushedMetal $t 128 132 138 99
Outline $t 1 88 92 98
Save $t (Join-Path $blockDir "controller_top.png")

# machine_geared_front: pulley + gearbox
$t = New-Tex 16 16
BrushedMetal $t 100 104 110 111
Outline $t 1 70 74 80
Circle $t 7.5 7.5 5.2 80 84 92
Circle $t 7.5 7.5 4.2 130 134 140
Circle $t 7.5 7.5 2.0 60 64 70
for ($i = 0; $i -lt 8; $i++) {
    $ang = $i * [Math]::PI / 4
    $x = [int][Math]::Round(7.5 + 3.2 * [Math]::Cos($ang))
    $y = [int][Math]::Round(7.5 + 3.2 * [Math]::Sin($ang))
    Px $t $x $y 90 94 100
}
Rect $t 2 13 13 14 70 74 80
Save $t (Join-Path $blockDir "machine_geared_front.png")

# machine_gearless_front: big smooth sheave
$t = New-Tex 16 16
BrushedMetal $t 96 100 106 122
Outline $t 1 66 70 76
Circle $t 7.5 7.5 6.0 70 74 82
Circle $t 7.5 7.5 5.0 150 154 160
Circle $t 7.5 7.5 4.6 170 174 180
Circle $t 7.5 7.5 1.6 110 114 120
for ($i = 0; $i -lt 12; $i++) {
    $ang = $i * [Math]::PI / 6
    $x = [int][Math]::Round(7.5 + 4.8 * [Math]::Cos($ang))
    $y = [int][Math]::Round(7.5 + 4.8 * [Math]::Sin($ang))
    Px $t $x $y 100 104 110
}
Save $t (Join-Path $blockDir "machine_gearless_front.png")

# mrl_drive: slim motor with fins
$t = New-Tex 16 16
BrushedMetal $t 118 122 128 133
Outline $t 1 84 88 94
Rect $t 2 4 13 10 60 64 70
for ($y = 5; $y -le 9; $y++) { HLine $t $y 130 134 140 }
Rect $t 10 5 13 10 40 44 50
Rect $t 11 6 12 9 200 170 40
Save $t (Join-Path $blockDir "mrl_drive.png")

# door_frame
$t = New-Tex 16 16
BrushedMetal $t 140 144 150 144
Outline $t 1 100 104 110
HLine $t 0 170 174 180
HLine $t 15 100 104 110
VLine $t 0 170 174 180
VLine $t 15 100 104 110
Save $t (Join-Path $blockDir "door_frame.png")

# call buttons: arrows
function CallButton([string]$name, [int]$dirUp, [bool]$lit) {
    $t = New-Tex 16 16
    BrushedMetal $t 118 122 128 155
    Outline $t 1 84 88 94
    Rect $t 3 3 12 12 40 44 50
    $c = if ($lit) { 255 } else { 150 }
    $base = if ($lit) { 240 } else { 130 }
    if ($dirUp -eq 1) {
        for ($i = 0; $i -lt 5; $i++) {
            $y = 10 - $i
            $half = $i
            Rect $t ([int](8 - $half)) $y ([int](8 + $half)) $y $base $c 60
        }
        Rect $t 7 10 8 10 $base $c 60
    } else {
        for ($i = 0; $i -lt 5; $i++) {
            $y = 5 + $i
            $half = 4 - $i
            Rect $t ([int](8 - $half)) $y ([int](8 + $half)) $y $base $c 60
        }
        Rect $t 7 9 8 9 $base $c 60
    }
    Save $t (Join-Path $blockDir $name)
}
CallButton "call_up.png" 1 $false
CallButton "call_up_lit.png" 1 $true
CallButton "call_down.png" 0 $false
CallButton "call_down_lit.png" 0 $true

# kiosk_front: screen + keypad
$t = New-Tex 16 16
BrushedMetal $t 124 128 134 166
Outline $t 1 88 92 98
Rect $t 2 1 13 7 15 20 28
Rect $t 3 2 12 6 40 170 210
for ($x = 3; $x -le 12; $x += 3) { Rect $t $x 2 $x 6 20 60 90 }
Rect $t 2 9 13 14 30 34 40
for ($y = 10; $y -le 13; $y += 2) {
    for ($x = 3; $x -le 12; $x += 3) { Px $t $x $y 200 205 210 }
}
Save $t (Join-Path $blockDir "kiosk_front.png")

# kiosk_side
$t = New-Tex 16 16
BrushedMetal $t 110 114 120 177
Outline $t 1 80 84 90
Save $t (Join-Path $blockDir "kiosk_side.png")

# indicator_front: dark display
$t = New-Tex 16 16
BrushedMetal $t 116 120 126 188
Outline $t 1 80 84 90
Rect $t 2 2 13 13 12 14 18
Rect $t 3 3 12 12 255 176 0
Rect $t 4 4 11 11 220 150 0
Save $t (Join-Path $blockDir "indicator_front.png")

# ---------------- item texture ----------------
# lift_linker: wand
$t = New-Tex 16 16
Fill-Noise $t 0 0 0 0 199 0
for ($i = 0; $i -lt 10; $i++) { Px $t (12 - $i) (13 - $i) 190 195 205 }
for ($i = 0; $i -lt 8; $i++) { Px $t (11 - $i) (13 - $i) 120 124 130 }
Rect $t 2 2 5 5 60 190 240
Px $t 3 3 240 250 255
Px $t 6 2 60 190 240
Px $t 2 6 60 190 240
Save $t (Join-Path $itemDir "lift_linker.png")

# ---------------- entity textures ----------------
# cab.png 64x160 brushed panels
$cab = New-Tex 64 160
Fill-Noise $cab 150 154 160 10 211
$rnd = New-Object System.Random(212)
for ($i = 0; $i -lt 220; $i++) {
    $y = $rnd.Next(0, 160)
    $x = $rnd.Next(0, 64)
    $len = $rnd.Next(3, 14)
    $d = $rnd.Next(-14, 15)
    for ($k = 0; $k -lt $len; $k++) {
        Px $cab (($x + $k) % 64) $y (Clamp (150 + $d)) (Clamp (154 + $d)) (Clamp (160 + $d))
    }
}
# panel seams
for ($y = 0; $y -lt 160; $y += 8) { HLine $cab $y 120 124 130 }
for ($x = 0; $x -lt 64; $x += 16) { VLine $cab $x 120 124 130 }
# door panel seam region (28..40 x 80..105)
Rect $cab 34 80 34 104 110 114 120
Save $cab (Join-Path $entDir "cab.png")

# door.png 32x40
$door = New-Tex 32 40
Fill-Noise $door 152 156 162 10 221
$rnd = New-Object System.Random(222)
for ($i = 0; $i -lt 120; $i++) {
    $y = $rnd.Next(0, 40)
    $x = $rnd.Next(0, 32)
    $len = $rnd.Next(3, 12)
    $d = $rnd.Next(-12, 13)
    for ($k = 0; $k -lt $len; $k++) {
        Px $door (($x + $k) % 32) $y (Clamp (152 + $d)) (Clamp (156 + $d)) (Clamp (162 + $d))
    }
}
Rect $door 0 0 31 0 110 114 120
Rect $door 0 39 31 39 110 114 120
Rect $door 0 0 0 39 110 114 120
Rect $door 31 0 31 39 110 114 120
# center seam at x=16 (UV region maps cube; draw subtle seam)
Rect $door 15 2 15 37 120 124 130
Rect $door 16 2 16 37 190 194 200
Save $door (Join-Path $entDir "door.png")

Write-Host "Textures generated."
Get-ChildItem -Recurse $root -File | Measure-Object | Select-Object Count
