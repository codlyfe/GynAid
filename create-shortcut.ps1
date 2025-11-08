
$WshShell = New-Object -comObject WScript.Shell
$Shortcut = $WshShell.CreateShortcut("$env:USERPROFILE\Desktop\Gynassist.lnk")
$Shortcut.TargetPath = "cmd.exe"
$Shortcut.Arguments = "/c cd /d \"c:\Users\Hp\Desktop\Gynassist\" && npm run dev"
$Shortcut.WorkingDirectory = "c:\Users\Hp\Desktop\Gynassist"
$Shortcut.IconLocation = "c:\Users\Hp\Desktop\Gynassist\assets\icon.ico"
$Shortcut.Description = "Reproductive Health Companion for Women"
$Shortcut.WindowStyle = 1
$Shortcut.Save()
