# Offline Gradle fallback

This project uses the public Gradle distribution URL by default. When the
public download fails because of a network timeout or connection error, the
workspace-local distribution can be used explicitly:

```powershell
.\tools\build-offline.ps1 build
```

The script resolves the workspace root from its own location, checks that
`reference_resources/gradle-9.8.0-bin.zip` exists, extracts it under
`caches/gradle/offline`, and stores all Gradle state under `caches/gradle`.
No absolute path is committed to the project.
