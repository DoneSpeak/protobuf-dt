/*
 * Copyright (c) 2011 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.validation;

import static com.google.eclipse.protobuf.protobuf.ProtobufPackage.Literals.*;
import static com.google.eclipse.protobuf.validation.Messages.*;
import static java.lang.String.format;
import static org.eclipse.xtext.util.Strings.isEmpty;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.*;
import org.eclipse.xtext.scoping.impl.ImportUriResolver;
import org.eclipse.xtext.validation.Check;

import com.google.eclipse.protobuf.protobuf.*;
import com.google.eclipse.protobuf.protobuf.Package;
import com.google.eclipse.protobuf.util.*;
import com.google.inject.Inject;

/**
 * @author alruiz@google.com (Alex Ruiz)
 */
public class ProtobufJavaValidator extends AbstractProtobufJavaValidator {

  public static final String SYNTAX_IS_NOT_PROTO2_ERROR_CODE = "syntaxIsNotProto2";
  public static final String INVALID_FIELD_TAG_NUMBER_ERROR_CODE = "invalidFieldTagNumber";
  public static final String MORE_THAN_ONE_PACKAGE_ERROR_CODE = "moreThanOnePackage";

  @Inject private FieldOptions fieldOptions;
  @Inject private ImportUriResolver uriResolver;
  @Inject private IQualifiedNameProvider qualifiedNameProvider;
  @Inject private Properties properties;

  @Check public void checkDefaultValueType(FieldOption option) {
    if (!fieldOptions.isDefaultValueOption(option)) return;
    Property property = (Property) option.eContainer();
    ValueRef defaultValue = option.getValue();
    if (properties.isString(property)) {
      if (defaultValue instanceof StringRef) return;
      error(expectedString, FIELD_OPTION__VALUE);
    }
    if (properties.isBool(property)) {
      if (defaultValue instanceof BooleanRef) return;
      error(expectedTrueOrFalse, FIELD_OPTION__VALUE);
    }
  }

  @Check public void checkImportIsResolved(Import anImport) {
    boolean isResolved = isResolved(anImport);
    if (isResolved) return;
    uriResolver.apply(anImport);
    isResolved = isResolved(anImport);
    if (isResolved) return;
    error(format(importNotFound, anImport.getImportURI()), IMPORT__IMPORT_URI);
  }

  private boolean isResolved(Import anImport) {
    String importUri = anImport.getImportURI();
    if (!isEmpty(importUri)) {
      URI uri = URI.createURI(importUri);
      if (!isEmpty(uri.scheme())) return true;
    }
    return false;
  }

  @Check public void checkSyntaxIsProto2(Syntax syntax) {
    String name = syntax.getName();
    if ("proto2".equals(name)) return;
    String msg = (name == null) ? expectedSyntaxIdentifier : format(unrecognizedSyntaxIdentifier, name);
    error(msg, syntax, SYNTAX__NAME, SYNTAX_IS_NOT_PROTO2_ERROR_CODE);
  }

  @Check public void checkTagNumberIsUnique(Field field) {
    if (isNameNull(field)) return; // we already show an error if name is null, no need to go further.
    long index = field.getIndex();
    EObject container = field.eContainer();
    if (container instanceof Message) {
      Message message = (Message) container;
      for (MessageElement element : message.getElements()) {
        if (!(element instanceof Field)) continue;
        Field other = (Field) element;
        if (other == field) break;
        if (other.getIndex() != index) continue;
        QualifiedName messageName = qualifiedNameProvider.getFullyQualifiedName(message);
        String msg = format(fieldNumberAlreadyUsed, index, messageName.toString(), other.getName());
        error(msg, field, FIELD__INDEX, INVALID_FIELD_TAG_NUMBER_ERROR_CODE);
        break;
      }
    }
  }

  @Check public void checkTagNumberIsGreaterThanZero(Field field) {
    if (isNameNull(field)) return; // we already show an error if name is null, no need to go further.
    long index = field.getIndex();
    if (index > 0) return;
    String msg = (index == 0) ? fieldNumbersMustBePositive : expectedFieldNumber;
    error(msg, field, FIELD__INDEX, INVALID_FIELD_TAG_NUMBER_ERROR_CODE);
  }

  @Check public void checkOnlyOnePackageDefinition(Package aPackage) {
    boolean firstFound = false;
    Protobuf root = (Protobuf) aPackage.eContainer();
    for (ProtobufElement e : root.getElements()) {
      if (e == aPackage) {
        if (firstFound) error(multiplePackages, aPackage, PACKAGE__NAME, MORE_THAN_ONE_PACKAGE_ERROR_CODE);
        return;
      }
      if (e instanceof Package && !firstFound) firstFound = true;
    }
  }

  private boolean isNameNull(Field field) {
    return field.getName() == null;
  }
}
