// Copyright 2015 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.service.project.model;

import com.intellij.openapi.util.text.StringUtil;
import com.twitter.intellij.pants.util.PantsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TargetAddressInfo {
  /**
   * Target addresses.
   */
  private String targetAddress;

  /**
   * Target type.
   */
  protected String target_type;
  /**
   * Target type.
   */
  protected Globs globs;

  /**
   * Pants target type
   */
  private String pants_target_type = null;

  private boolean is_code_gen;

  public TargetAddressInfo() {
  }

  @NotNull
  public Globs getGlobs() {
    return globs != null ? globs : Globs.EMPTY;
  }

  public void setGlobs(Globs globs) {
    this.globs = globs;
  }

  @Nullable
  public String getTargetType() {
    return target_type;
  }

  public void setTargetType(@NotNull String target_type) {
    this.target_type = target_type;
  }

  public String getTargetAddress() {
    return targetAddress;
  }

  public void setTargetAddress(String targetAddress) {
    this.targetAddress = targetAddress;
  }

  @Nullable
  public String getInternalPantsTargetType() {
    return pants_target_type;
  }


  public boolean isScala() {
    return StringUtil.equals("scala_library", getInternalPantsTargetType()) || hasScalaSources();
  }

  public boolean isAnnotationProcessor() {
    return StringUtil.equals("annotation_processor", getInternalPantsTargetType());
  }

  public boolean isJarLibrary() {
    return StringUtil.equals("jar_library", getInternalPantsTargetType());
  }

  public boolean hasScalaSources() {
    return getGlobs().hasFileExtension("scala");
  }

  public String getCanonicalId() {
    return PantsUtil.getCanonicalTargetId(targetAddress);
  }
}
