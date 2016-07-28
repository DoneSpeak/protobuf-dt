/*
 * Copyright (c) 2011 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.scoping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import static com.google.eclipse.protobuf.junit.IEObjectDescriptions.descriptionsIn;
import static com.google.eclipse.protobuf.junit.core.IntegrationTestModule.integrationTestModule;
import static com.google.eclipse.protobuf.junit.core.XtextRule.overrideRuntimeModuleWith;
import static com.google.eclipse.protobuf.junit.matchers.ContainAllNames.containAll;
import static com.google.eclipse.protobuf.protobuf.ProtobufPackage.Literals.COMPLEX_TYPE;
import static com.google.eclipse.protobuf.protobuf.ProtobufPackage.Literals.COMPLEX_TYPE_LINK;
import static com.google.eclipse.protobuf.protobuf.ProtobufPackage.Literals.COMPLEX_TYPE_LINK__TARGET;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.IScope;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.eclipse.protobuf.junit.core.XtextRule;
import com.google.eclipse.protobuf.protobuf.ComplexType;
import com.google.eclipse.protobuf.protobuf.ComplexTypeLink;
import com.google.eclipse.protobuf.protobuf.MessageField;
import com.google.inject.Inject;

/**
 * Tests for <code>{@link ProtobufScopeProvider#getScope(ComplexTypeLink, EReference)}</code>
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class ProtobufScopeProvider_scope_ComplexTypeLink_target_Test {
  @Rule public XtextRule xtext = overrideRuntimeModuleWith(integrationTestModule());

  @Inject private EReference reference;
  @Inject private ProtobufScopeProvider scopeProvider;

  @Before
  public void setup() {
    when(reference.getEReferenceType()).thenReturn(COMPLEX_TYPE);
    when(reference.getEContainingClass()).thenReturn(COMPLEX_TYPE_LINK);
  }

  // syntax = "proto2";
  // package com.google.proto;
  //
  // enum Type {
  //   PERSONAL = 0;
  //   BUSINESS = 1;
  // }
  //
  // message Address {
  //   optional int32 number = 1;
  //   optional string street = 2;
  //   optional string city = 3;
  //   optional int32 zipCode = 4;
  // }
  //
  // message Contact {
  //   optional Type type = 1;
  // }
  @Test public void should_provide_Types() {
    MessageField field = xtext.find("type", MessageField.class);
    IScope scope = scopeProvider.getScope(typeOf(field), reference);
    assertThat(descriptionsIn(scope), containAll("Type", "proto.Type", "google.proto.Type",
        "com.google.proto.Type", "Address", "proto.Address", "google.proto.Address",
        "com.google.proto.Address", "Contact", "proto.Contact", "google.proto.Contact",
        "com.google.proto.Contact"));
  }

  // // Create file types.proto
  //
  // syntax = "proto2";
  // package test.proto;
  //
  // enum Type {
  //   PERSONAL = 0;
  //   BUSINESS = 1;
  // }
  //
  // message Address {
  //   optional int32 number = 1;
  //   optional string street = 2;
  //   optional string city = 3;
  //   optional int32 zipCode = 4;
  // }

  // syntax = "proto2";
  // package com.google.proto;
  //
  // import "types.proto";
  //
  // message Contact {
  //   optional com.google.test.Type type = 1;
  // }
  @Test public void should_provide_imported_Types() {
    MessageField field = xtext.find("type", " =", MessageField.class);
    IScope scope = scopeProvider.getScope(typeOf(field), reference);
    assertThat(descriptionsIn(scope), containAll("test.proto.Type", "test.proto.Address",
        "Contact", "proto.Contact", "google.proto.Contact", "com.google.proto.Contact"));
  }

  // // Create file types.proto
  //
  // syntax = "proto2";
  // package com.google.proto;
  //
  // enum Type {
  //   PERSONAL = 0;
  //   BUSINESS = 1;
  // }
  //
  // message Address {
  //   optional int32 number = 1;
  //   optional string street = 2;
  //   optional string city = 3;
  //   optional int32 zipCode = 4;
  // }

  // syntax = "proto2";
  // package com.google.proto;
  //
  // import "types.proto";
  //
  // message Contact {
  //   optional com.google.test.Type type = 1;
  // }
  @Test public void should_provide_imported_Types_with_equal_package() {
    MessageField field = xtext.find("type", " =", MessageField.class);
    IScope scope = scopeProvider.getScope(typeOf(field), reference);
    assertThat(descriptionsIn(scope), containAll("Type", "proto.Type", "google.proto.Type",
        "com.google.proto.Type", "Address", "proto.Address", "google.proto.Address",
        "com.google.proto.Address", "Contact", "proto.Contact", "google.proto.Contact",
        "com.google.proto.Contact"));
  }

  // // Create file types.proto
  //
  // syntax = "proto2";
  // package test.proto;
  //
  // enum Type {
  //   PERSONAL = 0;
  //   BUSINESS = 1;
  // }
  //
  // message Address {
  //   optional int32 number = 1;
  //   optional string street = 2;
  //   optional string city = 3;
  //   optional int32 zipCode = 4;
  // }

  // syntax = "proto2";
  // package com.google.proto;
  //
  // import public "types.proto";
  //
  // message Contact {
  //   optional test.proto.Type type = 1;
  // }
  @Test public void should_provide_public_imported_Types() {
    MessageField field = xtext.find("type", " =", MessageField.class);
    IScope scope = scopeProvider.getScope(typeOf(field), reference);
    assertThat(descriptionsIn(scope), containAll("test.proto.Type", "test.proto.Address",
        "Contact", "proto.Contact", "google.proto.Contact", "com.google.proto.Contact"));
  }

  // // Create file public-types.proto
  //
  // syntax = "proto2";
  // package test.proto;
  //
  // enum Type {
  //   PERSONAL = 0;
  //   BUSINESS = 1;
  // }
  //
  // message Address {
  //   optional int32 number = 1;
  //   optional string street = 2;
  //   optional string city = 3;
  //   optional int32 zipCode = 4;
  // }

  // // Create file types.proto
  //
  // syntax = "proto2";
  // package com.google.proto;
  //
  // import public "public-types.proto";

  // syntax = "proto2";
  // package com.google.proto;
  //
  // import "types.proto";
  //
  // message Contact {
  //   optional test.proto.Type type = 1;
  // }
  @Test public void should_provide_public_imported_Types_with_more_than_one_level() {
    MessageField field = xtext.find("type", " =", MessageField.class);
    IScope scope = scopeProvider.getScope(typeOf(field), reference);
    assertThat(descriptionsIn(scope), containAll("test.proto.Type", "test.proto.Address",
        "Contact", "proto.Contact", "google.proto.Contact", "com.google.proto.Contact"));
  }

  // // Create file sample_proto.proto
  // syntax = "proto2";
  // package sample.proto;
  //
  //  enum Fruit {
  //    GRAPE =  1;
  //  }

  //  syntax = "proto2";
  //  package sample.proto.foo;
  //  import "sample_proto.proto";
  //
  //  enum Fruit {
  //     GRAPE =  1;
  //  }
  //
  //  message Fruits {
  //   optional Fruit grape = 1;
  // }
  @Test public void should_provide_nearest_ComplexType() {
    MessageField field = xtext.find("grape", " =", MessageField.class);
    ComplexTypeLink link = (ComplexTypeLink) field.getType();
    ComplexType scopedEnum = link.getTarget();  // Enum inherits from ComplexType
    IScope scope = scopeProvider.getScope(field, COMPLEX_TYPE_LINK__TARGET);
    Object expectedEnum =
        scope.getSingleElement(
            QualifiedName.create("sample", "proto", "foo", "Fruit")).getEObjectOrProxy();
    assertEquals(expectedEnum, scopedEnum);
  }

  private static ComplexTypeLink typeOf(MessageField field) {
    return (ComplexTypeLink) field.getType();
  }
}