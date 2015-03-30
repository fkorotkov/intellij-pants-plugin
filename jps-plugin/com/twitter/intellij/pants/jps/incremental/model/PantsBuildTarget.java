// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.jps.incremental.model;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.Processor;
import com.twitter.intellij.pants.jps.incremental.serialization.PantsJpsModelSerializerExtension;
import com.twitter.intellij.pants.util.PantsConstants;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildRootIndex;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetRegistry;
import org.jetbrains.jps.builders.TargetOutputIndex;
import org.jetbrains.jps.builders.java.JavaModuleBuildTargetType;
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ModuleBuildTarget;
import org.jetbrains.jps.indices.IgnoredFileIndex;
import org.jetbrains.jps.indices.ModuleExcludeIndex;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.module.JpsModule;

import java.io.File;
import java.util.*;

public class PantsBuildTarget extends BuildTarget<PantsSourceRootDescriptor> {
  @NotNull
  private final Set<String> myTargetAddresses;
  @NotNull
  private final String myRootTarget;

  protected PantsBuildTarget(@NotNull String rootTarget, @NotNull Set<String> addresses) {
    super(PantsBuildTargetType.INSTANCE);
    myRootTarget = rootTarget;
    myTargetAddresses = addresses;
  }

  @Override
  public String getId() {
    return PantsConstants.PANTS;
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return "Pants Target";
  }

  @Override
  public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
    return Collections.emptyList();
  }

  @NotNull
  @Override
  public List<PantsSourceRootDescriptor> computeRootDescriptors(
    final JpsModel model,
    final ModuleExcludeIndex index,
    final IgnoredFileIndex ignoredFileIndex,
    final BuildDataPaths dataPaths
  ) {
    final Set<PantsSourceRootDescriptor> result = new HashSet<PantsSourceRootDescriptor>();
    processJavaModuleTargets(
      model.getProject(),
      new Processor<ModuleBuildTarget>() {
        @Override
        public boolean process(ModuleBuildTarget target) {
          final JpsPantsModuleExtension moduleExtension = PantsJpsModelSerializerExtension.findPantsModuleExtension(target.getModule());
          final String targetAddress = moduleExtension != null ? moduleExtension.getTargetAddress() : null;
          final List<JavaSourceRootDescriptor> descriptors = target.computeRootDescriptors(model, index, ignoredFileIndex, dataPaths);
          for (JavaSourceRootDescriptor javaSourceRootDescriptor : descriptors) {
            result.add(
              new PantsSourceRootDescriptor(
                PantsBuildTarget.this,
                targetAddress,
                javaSourceRootDescriptor.getRootFile(),
                javaSourceRootDescriptor.isGenerated(),
                javaSourceRootDescriptor.getExcludedRoots()
              )
            );
          }
          return true;
        }
      }
    );
    return new ArrayList<PantsSourceRootDescriptor>(result);
  }

  @NotNull
  @Override
  public Collection<File> getOutputRoots(final CompileContext context) {
    final Set<File> result = new THashSet<File>(FileUtil.FILE_HASHING_STRATEGY);
    final JpsProject jpsProject = context.getProjectDescriptor().getProject();
    processJavaModuleTargets(
      jpsProject,
      new Processor<ModuleBuildTarget>() {
        @Override
        public boolean process(ModuleBuildTarget target) {
          result.addAll(target.getOutputRoots(context));
          return true;
        }
      }
    );
    return result;
  }

  private void processJavaModuleTargets(@NotNull JpsProject jpsProject, @NotNull Processor<ModuleBuildTarget> processor) {
    for (JpsModule module : jpsProject.getModules()) {
      for (JavaModuleBuildTargetType buildTargetType : JavaModuleBuildTargetType.ALL_TYPES) {
        final ModuleBuildTarget moduleBuildTarget = new ModuleBuildTarget(module, buildTargetType);
        if (!processor.process(moduleBuildTarget)) {
          return;
        }
      }
    }
  }

  @Nullable
  @Override
  public PantsSourceRootDescriptor findRootDescriptor(String rootId, BuildRootIndex rootIndex) {
    return null;
  }

  @NotNull
  public String getRootTarget() {
    return myRootTarget;
  }

  @NotNull
  public Set<String> getTargetAddresses() {
    return myTargetAddresses;
  }

  @Override
  public String toString() {
    return "PantsBuildTarget{" +
           "myTargetAddresses=" + myTargetAddresses +
           ", myRootTarget='" + myRootTarget + '\'' +
           '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PantsBuildTarget target = (PantsBuildTarget)o;

    if (!myTargetAddresses.equals(target.myTargetAddresses)) return false;
    return myRootTarget.equals(target.myRootTarget);
  }

  @Override
  public int hashCode() {
    int result = myTargetAddresses.hashCode();
    result = 31 * result + myRootTarget.hashCode();
    return result;
  }
}
