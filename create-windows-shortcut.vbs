Set WshShell = CreateObject("WScript.Shell")
Set oShellLink = WshShell.CreateShortcut(WshShell.SpecialFolders("Desktop") & "\Gynassist.lnk")
oShellLink.TargetPath = "cmd.exe"
oShellLink.Arguments = "/c cd /d ""C:\Users\Hp\Desktop\Gynassist"" && npm run dev"
oShellLink.WorkingDirectory = "C:\Users\Hp\Desktop\Gynassist"
oShellLink.Description = "Gynassist - Reproductive Health Companion"
oShellLink.WindowStyle = 1
oShellLink.Save

' Create Start Menu shortcut
Set oStartMenuLink = WshShell.CreateShortcut(WshShell.SpecialFolders("StartMenu") & "\Programs\Gynassist.lnk")
oStartMenuLink.TargetPath = "cmd.exe"
oStartMenuLink.Arguments = "/c cd /d ""C:\Users\Hp\Desktop\Gynassist"" && npm run dev"
oStartMenuLink.WorkingDirectory = "C:\Users\Hp\Desktop\Gynassist"
oStartMenuLink.Description = "Gynassist - Reproductive Health Companion"
oStartMenuLink.WindowStyle = 1
oStartMenuLink.Save

WScript.Echo "Gynassist shortcuts created successfully!"