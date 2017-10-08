;
; 	Copyright (C) 2017 - Wadim Halle (e-mail: wadim-h@hotmail.de)
; 
;    This program is free software: you can redistribute it and/or modify
;    it under the terms of the GNU General Public License as published by
;    the Free Software Foundation, either version 3 of the License, or
;    (at your option) any later version.
;
;    This program is distributed in the hope that it will be useful,
;    but WITHOUT ANY WARRANTY; without even the implied warranty of
;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;    GNU General Public License for more details.
;
;    You should have received a copy of the GNU General Public License
;    along with this program.  If not, see <http://www.gnu.org/licenses/>.



;This file will be executed next to the application bundle image
;I.e. current directory will contain folder VideoEditor with application files
[Setup]
AppId={{fxApplication}}
AppName=VideoEditor
AppVersion=1.0
AppVerName=VideoEditor 1.0
AppPublisher=Wadim Halle
AppComments=DVR
AppCopyright=Copyright (C) 2017 - Wadim Halle
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
DefaultDirName={pf}\VideoEditor
DisableStartupPrompt=Yes
DisableDirPage=NO
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=Wadim Halle
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=VideoEditor-1.0
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=VideoEditor\VideoEditor.ico
UninstallDisplayIcon={app}\VideoEditor.ico
UninstallDisplayName=Video Editor
WizardImageStretch=No
WizardSmallImageFile=VideoEditor-setup-icon.bmp
ArchitecturesInstallIn64BitMode=


[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "german"; MessagesFile: "compiler:Languages\German.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "VideoEditor\VideoEditor.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "VideoEditor\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\VideoEditor"; Filename: "{app}\VideoEditor.exe"; IconFilename: "{app}\VideoEditor.ico"; Check: returnTrue()
Name: "{commondesktop}\VideoEditor"; Filename: "{app}\VideoEditor.exe"; Tasks: desktopicon


[Run]
Filename: "{app}\VideoEditor.exe"; Parameters: "-Xappcds:generatecache"; Check: returnFalse()
Filename: "{app}\VideoEditor.exe"; Description: "{cm:LaunchProgram,VideoEditor}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\VideoEditor.exe"; Parameters: "-install -svcName ""VideoEditor"" -svcDesc ""VideoEditor"" -mainExe ""VideoEditor""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\VideoEditor "; Parameters: "-uninstall -svcName VideoEditor -stopOnUninstall"; Check: returnFalse()

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
