#!/bin/bash

# Read the settings.gradle file
modules=$(grep 'include(' settings.gradle.kts | sed 's/include(//;s/)//;s/,//g' | tr -d '[:space:]' | tr ':' '\n' | sed '/^$/d')

# Prepare the command
cmd="./gradlew"
for module in $modules; do
    cmd="$cmd $module:dependencies"
done

# Redirect the output to dependencies.txt
$cmd > dependencies.txt