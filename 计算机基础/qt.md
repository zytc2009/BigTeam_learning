

在MacOS、Linux和windows-mingw环境：

```
* {
    QMAKE_CFLAGS_RELEASE += -g
    QMAKE_CXXFLAGS_RELEASE += -g
    QMAKE_CFLAGS_RELEASE -= -O2
    QMAKE_CXXFLAGS_RELEASE -= -O2
    #QMAKE_LFLAGS_RELEASE = -mthreads -Wl
}
```

windows-msvc：

```
win32-msvc* {
    QMAKE_CFLAGS_RELEASE += /Zi
    QMAKE_LFLAGS_RELEASE += /MAP /DEBUG /opt:ref /INCREMENTAL:NO
}
```

> QMAKE_CXXFLAGS_RELEASE = $$QMAKE_CFLAGS_RELEASE_WITH_DEBUGINFO QMAKE_LFLAGS_RELEASE = $$QMAKE_LFLAGS_RELEASE_WITH_DEBUGINFO

3.接入BreakPad

```
  BreakpadHandler *breakpad_instance = BreakpadHandler::GetInstance();
    QStringList path_list2 = QStandardPaths::standardLocations(QStandardPaths::DataLocation);
    QString std_base_path =  path_list2[0];
    QString my_crash_path = std_base_path + "/crashs";
    breakpad_instance->SetupBreakPad(my_crash_path);//or just using "crashes"
```

