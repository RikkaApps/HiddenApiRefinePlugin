# HiddenApiRefinePlugin

A Gradle plugin that improves the experience when developing Android apps, especially system tools, that use hidden APIs.

## Background

When developing system tools, it's impossible inevitable to use hidden APIs. There are two ways to use hidden APIs, the reflection way and the linking way. For some types of system tools, a large amount of hidden APIs is required, the reflection way is too inconvenient. So the linking way is commonly used in those projects.

In short, the linking way is to create Java-only modules with stub classes and then `compileOnly` them in the main Android modules. This is the same as what Android SDK's `android.jar` does.

However the linking way, or "the stub way", have some problems:

1. "Bridge classes" is required if only some members of the class are hidden.
2. Kotlin will try to link public classes from the stub module, `implementation` a second Java-only which `compileOnly` the stub module can workaround the problem.
3. Interface implementation will be removed by R8 if the stub module is not `compileOnly` directly from the main module, however doing this will bring back problem 2.

This plugin is to solve these problems.

## What do this plugin do

This plugin uses the Transform API of the Android Gradle Plugin to create a transform that removes specific prefixes from class names. So we can add a special prefix (e.g., `'$'`) to stub classes, and all the problems will be gone and the stub module does not need to be a Java-only module.

The idea is from [@Kr328](https://github.com/Kr328).

## Usage

### Root project

```gradle
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'dev.rikka.tools:hidden-api-refine:1.0.0'
    }
}
```

### Module

```gradle
plugins {
    id('dev.rikka.tools.hidden-api-refine')
}

hiddenApiRefine {
    // Prefix to remove, default is ['$']
    refinePrefix = ['$']
    // Print log, default is true
    log = true
}
```

Note, the plugin is to remove the prefix of the package name, not the class name. For example, the `$` in `com.example.$Example` will not be removed.

### Use "hidden" classes with public classes

Sometimes we need to use "hidden" classes with public classes.

For example, use the hidden `android.os.UserHandle#of` method to create an instance of `UserHandle`.

```
package $android.os;

public class UserHandle {
    public static UserHandle ALL;

    public static UserHandle of(int userId) {
        throw new RuntimeException();
    }
}
```

```
$android.os.UserHandle userHandle = $android.os.UserHandle.of(userId);
```

However, this `UserHandle` cannot be passed to other public APIs that accepts `UserHandle`. This is because this `UserHandle` is actually `$android.os.UserHandle` rather than `android.os.UserHandle`.

This can be simply solved with this "trick".

```
public class Unsafe {
    @SuppressWarnings("unchecked")
    public static <T> T unsafeCast(Object object) {
        return (T) object;
    }
}
```

```
UserHandle userHandle = Unsafe.unsafeCast($android.os.UserHandle.of(userId));
```

After R8, this "cast" will be "removed" or "inlined". This line will be identical to a normal method call.

```
UserHandle userHandle = android.os.UserHandle.of(userId);
```
