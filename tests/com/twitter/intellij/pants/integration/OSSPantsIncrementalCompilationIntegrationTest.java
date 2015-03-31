// Copyright 2015 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.integration;

import com.twitter.intellij.pants.settings.PantsSettings;
import com.twitter.intellij.pants.testFramework.OSSPantsIntegrationTest;

public class OSSPantsIncrementalCompilationIntegrationTest extends OSSPantsIntegrationTest {
  public OSSPantsIncrementalCompilationIntegrationTest() {
    super(false);
  }

  public void testHelloByTargetName() throws Throwable {
    if (PantsSettings.getInstance(myProject).isCompileWithIntellij()) {
      return;
    }
    doImport("examples/src/scala/com/pants/example/hello/BUILD", "hello");

    assertModules(
      "examples_src_resources_com_pants_example_hello_hello",
      "examples_src_scala_com_pants_example_hello_module",
      "examples_src_scala_com_pants_example_hello_hello",
      "examples_src_scala_com_pants_example_hello_welcome_welcome",
      "examples_src_java_com_pants_examples_hello_greet_greet",
      "examples_src_scala_com_pants_example_hello_exe_exe"
    );

    assertContain(makeProject(), "pants: Recompiling all 6 targets");
    assertContain(makeProject(), "pants: No changes to compile.");

    modify("com.pants.example.hello.exe.Exe");

    assertContain(makeProject(), "pants: Recompiling examples/src/scala/com/pants/example/hello/exe:exe");

    modify("com.pants.example.hello.exe.Exe");
    modify("com.pants.example.hello.welcome.WelcomeEverybody");

    assertContain(makeProject(), "pants: Recompiling 2 targets");
  }
}
