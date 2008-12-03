/*
 * Copyright 2000-2007 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.psi.util;

import com.intellij.psi.*;
import com.intellij.util.StringBuilderSpinAllocator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

/**
 *
 */
public class PsiFormatUtil {
  public static final int SHOW_NAME = 0x0001; // variable, method, class
  public static final int SHOW_TYPE = 0x0002; // variable, method
  public static final int TYPE_AFTER = 0x0004; // variable, method
  public static final int SHOW_MODIFIERS = 0x0008; // variable, method, class
  public static final int MODIFIERS_AFTER = 0x0010; // variable, method, class
  public static final int SHOW_REDUNDANT_MODIFIERS = 0x0020; // variable, method, class, modifier list
  public static final int SHOW_PACKAGE_LOCAL = 0x0040; // variable, method, class, modifier list
  public static final int SHOW_INITIALIZER = 0x0080; // variable
  public static final int SHOW_PARAMETERS = 0x0100; // method
  public static final int SHOW_THROWS = 0x0200; // method
  public static final int SHOW_EXTENDS_IMPLEMENTS = 0x0400; // class
  public static final int SHOW_FQ_NAME = 0x0800; // class, field, method
  public static final int SHOW_CONTAINING_CLASS = 0x1000; // field, method
  public static final int SHOW_FQ_CLASS_NAMES = 0x2000; // variable, method, class
  public static final int JAVADOC_MODIFIERS_ONLY = 0x4000; // field, method, class
  public static final int SHOW_ANONYMOUS_CLASS_VERBOSE = 0x8000; // class
  public static final int SHOW_RAW_TYPE = 0x10000; //type
  public static final int MAX_PARAMS_TO_SHOW = 7;

  public static String formatVariable(PsiVariable variable, int options, PsiSubstitutor substitutor){
    StringBuilder buffer = new StringBuilder();
    if ((options & SHOW_MODIFIERS) != 0 && (options & MODIFIERS_AFTER) == 0){
      buffer.append(formatModifiers(variable, options));
    }
    if ((options & SHOW_TYPE) != 0 && (options & TYPE_AFTER) == 0){
      if (buffer.length() > 0){
        buffer.append(' ');
      }
      buffer.append(formatType(variable.getType(), options, substitutor));
    }
    if (variable instanceof PsiField && (options & SHOW_CONTAINING_CLASS) != 0){
      PsiClass aClass = ((PsiField)variable).getContainingClass();
      if (aClass != null){
        if (buffer.length() > 0){
          buffer.append(' ');
        }
        String className = aClass.getName();
        if (className != null) {
          if ((options & SHOW_FQ_NAME) != 0){
            String qName = aClass.getQualifiedName();
            if (qName != null){
              buffer.append(qName);
            }
            else{
              buffer.append(className);
            }
          }
          else{
            buffer.append(className);
          }
          buffer.append('.');
        }
      }
      if ((options & SHOW_NAME) != 0){
        buffer.append(variable.getName());
      }
    }
    else{
      if ((options & SHOW_NAME) != 0){
        String name = variable.getName();
        if (name != null){
          if (buffer.length() > 0){
            buffer.append(' ');
          }
          buffer.append(name);
        }
      }
    }
    if ((options & SHOW_TYPE) != 0 && (options & TYPE_AFTER) != 0){
      if ((options & SHOW_NAME) != 0 && variable.getName() != null){
        buffer.append(':');
      }
      buffer.append(formatType(variable.getType(), options, substitutor));
    }
    if ((options & SHOW_MODIFIERS) != 0 && (options & MODIFIERS_AFTER) != 0){
      String modifiers = formatModifiers(variable, options);
      if (modifiers.length() > 0){
        if (buffer.length() > 0){
          buffer.append(' ');
        }
        buffer.append(modifiers);
      }
    }
    if ((options & SHOW_INITIALIZER) != 0){
      PsiExpression initializer = variable.getInitializer();
      if (initializer != null){
        buffer.append(" = ");
        String text = initializer.getText();
        int index1 = text.lastIndexOf('\n');
        if (index1 < 0) index1 = text.length();
        int index2 = text.lastIndexOf('\r');
        if (index2 < 0) index2 = text.length();
        int index = Math.min(index1, index2);
        buffer.append(text.substring(0, index));
        if (index < text.length()) {
          buffer.append(" ...");
        }
      }
    }
    return buffer.toString();
  }

  public static String formatMethod(PsiMethod method, PsiSubstitutor substitutor, int options, int parameterOptions){
    return formatMethod(method, substitutor, options, parameterOptions, MAX_PARAMS_TO_SHOW);
  }

  public static String formatMethod(PsiMethod method, PsiSubstitutor substitutor, int options, int parameterOptions, int maxParametersToShow){
    StringBuilder buffer = new StringBuilder();
    if ((options & SHOW_MODIFIERS) != 0 && (options & MODIFIERS_AFTER) == 0){
      buffer.append(formatModifiers(method, options));
    }
    if ((options & SHOW_TYPE) != 0 && (options & TYPE_AFTER) == 0){
      PsiType type = method.getReturnType();
      if (type != null){
        if (buffer.length() > 0){
          buffer.append(' ');
        }
        buffer.append(formatType(type, options, substitutor));
      }
    }
    if ((options & SHOW_CONTAINING_CLASS) != 0){
      PsiClass aClass = method.getContainingClass();
      if (aClass != null){
        if (buffer.length() > 0){
          buffer.append(' ');
        }
        String name = aClass.getName();
        if (name != null) {
          if ((options & SHOW_FQ_NAME) != 0){
            String qName = aClass.getQualifiedName();
            if (qName != null){
              buffer.append(qName);
            }
            else{
              buffer.append(name);
            }
          }
          else{
            buffer.append(name);
          }
          buffer.append('.');
        }
      }
      if ((options & SHOW_NAME) != 0){
        buffer.append(method.getName());
      }
    }
    else{
      if ((options & SHOW_NAME) != 0){
        if (buffer.length() > 0){
          buffer.append(' ');
        }
        buffer.append(method.getName());
      }
    }
    if ((options & SHOW_PARAMETERS) != 0){
      buffer.append('(');
      PsiParameter[] parms = method.getParameterList().getParameters();
      for(int i = 0; i < Math.min(parms.length, maxParametersToShow); i++) {
        PsiParameter parm = parms[i];
        if (i > 0){
          buffer.append(", ");
        }
        buffer.append(formatVariable(parm, parameterOptions, substitutor));
      }
      if(parms.length > maxParametersToShow) {
        buffer.append (", ...");
      }
      buffer.append(')');
    }
    if ((options & SHOW_TYPE) != 0 && (options & TYPE_AFTER) != 0){
      PsiType type = method.getReturnType();
      if (type != null){
        if (buffer.length() > 0){
          buffer.append(':');
        }
        buffer.append(formatType(type, options, substitutor));
      }
    }
    if ((options & SHOW_MODIFIERS) != 0 && (options & MODIFIERS_AFTER) != 0){
      String modifiers = formatModifiers(method, options);
      if (modifiers.length() > 0){
        if (buffer.length() > 0){
          buffer.append(' ');
        }
        buffer.append(modifiers);
      }
    }
    if ((options & SHOW_THROWS) != 0){
      String throwsText = formatReferenceList(method.getThrowsList(), options);
      if (throwsText.length() > 0){
        if (buffer.length() > 0){
          buffer.append(' ');
        }
        //noinspection HardCodedStringLiteral
        buffer.append("throws ");
        buffer.append(throwsText);
      }
    }
    return buffer.toString();
  }

  @NotNull public static String formatClass(@NotNull PsiClass aClass, int options){
    StringBuilder buffer = new StringBuilder();
    if ((options & SHOW_MODIFIERS) != 0 && (options & MODIFIERS_AFTER) == 0){
      buffer.append(formatModifiers(aClass, options));
    }
    if ((options & SHOW_NAME) != 0){
      if (aClass instanceof PsiAnonymousClass && (options & SHOW_ANONYMOUS_CLASS_VERBOSE) != 0) {
        final PsiClassType baseClassReference = ((PsiAnonymousClass) aClass).getBaseClassType();
        PsiClass baseClass = baseClassReference.resolve();
        String name = baseClass == null ? baseClassReference.getPresentableText() : formatClass(baseClass, options);
        buffer.append(PsiBundle.message("anonymous.class.derived.display", name));
      }
      else {
        String name = aClass.getName();
        if (name != null) {
          if (buffer.length() > 0) {
            buffer.append(' ');
          }
          if ((options & SHOW_FQ_NAME) != 0) {
            String qName = aClass.getQualifiedName();
            if (qName != null) {
              buffer.append(qName);
            }
            else {
              buffer.append(aClass.getName());
            }
          }
          else {
            buffer.append(aClass.getName());
          }
        }
      }
    }
    if ((options & SHOW_MODIFIERS) != 0 && (options & MODIFIERS_AFTER) != 0){
      String modifiers = formatModifiers(aClass, options);
      if (modifiers.length() > 0){
        if (buffer.length() > 0){
          buffer.append(' ');
        }
        buffer.append(modifiers);
      }
    }
    if ((options & SHOW_EXTENDS_IMPLEMENTS) != 0){
      String extendsText = formatReferenceList(aClass.getExtendsList(), options);
      if (extendsText.length() > 0){
        if (buffer.length() > 0){
          buffer.append(' ');
        }
        //noinspection HardCodedStringLiteral
        buffer.append("extends ");
        buffer.append(extendsText);
      }
      String implementsText = formatReferenceList(aClass.getImplementsList(), options);
      if (implementsText.length() > 0){
        if (buffer.length() > 0){
          buffer.append(' ');
        }
        //noinspection HardCodedStringLiteral
        buffer.append("implements ");
        buffer.append(implementsText);
      }
    }
    return buffer.toString();
  }

  public static String formatModifiers(PsiElement element, int options) throws IllegalArgumentException{
    PsiModifierList list;
    boolean isInterface = false;
    if (element instanceof PsiVariable){
      list = ((PsiVariable)element).getModifierList();
    }
    else if (element instanceof PsiMethod){
      list = ((PsiMethod)element).getModifierList();
    }
    else if (element instanceof PsiClass){
      isInterface = ((PsiClass)element).isInterface();
      list = ((PsiClass)element).getModifierList();
      if (list == null) return "";
    }
    else if (element instanceof PsiClassInitializer){
      list = ((PsiClassInitializer)element).getModifierList();
      if (list == null) return "";
    }
    else{
      throw new IllegalArgumentException();
    }
    if (list == null) return "";
    StringBuilder buffer = new StringBuilder();
    if ((options & SHOW_REDUNDANT_MODIFIERS) != 0
        ? list.hasModifierProperty(PsiModifier.PUBLIC)
        : list.hasExplicitModifier(PsiModifier.PUBLIC)) appendModifier(buffer, PsiModifier.PUBLIC);

    if (list.hasModifierProperty(PsiModifier.PROTECTED)){
      appendModifier(buffer, PsiModifier.PROTECTED);
    }
    if (list.hasModifierProperty(PsiModifier.PRIVATE)){
      appendModifier(buffer, PsiModifier.PRIVATE);
    }

    if ((options & SHOW_REDUNDANT_MODIFIERS) != 0
        ? list.hasModifierProperty(PsiModifier.PACKAGE_LOCAL)
        : list.hasExplicitModifier(PsiModifier.PACKAGE_LOCAL)) {
      if (element instanceof PsiClass && element.getParent() instanceof PsiDeclarationStatement) {// local class
        appendModifier(buffer, PsiBundle.message("local.class.preposition"));
      }
      else {
        appendModifier(buffer, PsiBundle.visibilityPresentation(PsiModifier.PACKAGE_LOCAL));
      }
    }

    if ((options & SHOW_REDUNDANT_MODIFIERS) != 0
        ? list.hasModifierProperty(PsiModifier.STATIC)
        : list.hasExplicitModifier(PsiModifier.STATIC)) appendModifier(buffer, PsiModifier.STATIC);

    if (!isInterface && //cls modifier list
        ((options & SHOW_REDUNDANT_MODIFIERS) != 0
        ? list.hasModifierProperty(PsiModifier.ABSTRACT)
        : list.hasExplicitModifier(PsiModifier.ABSTRACT))) appendModifier(buffer, PsiModifier.ABSTRACT);

    if ((options & SHOW_REDUNDANT_MODIFIERS) != 0
        ? list.hasModifierProperty(PsiModifier.FINAL)
        : list.hasExplicitModifier(PsiModifier.FINAL)) appendModifier(buffer, PsiModifier.FINAL);

    if (list.hasModifierProperty(PsiModifier.NATIVE) && (options & JAVADOC_MODIFIERS_ONLY) == 0){
      appendModifier(buffer, PsiModifier.NATIVE);
    }
    if (list.hasModifierProperty(PsiModifier.SYNCHRONIZED) && (options & JAVADOC_MODIFIERS_ONLY) == 0){
      appendModifier(buffer, PsiModifier.SYNCHRONIZED);
    }
    if (list.hasModifierProperty(PsiModifier.STRICTFP) && (options & JAVADOC_MODIFIERS_ONLY) == 0){
      appendModifier(buffer, PsiModifier.STRICTFP);
    }
    if (list.hasModifierProperty(PsiModifier.TRANSIENT) &&
        element instanceof PsiVariable // javac 5 puts transient attr for methods
       ){
      appendModifier(buffer, PsiModifier.TRANSIENT);
    }
    if (list.hasModifierProperty(PsiModifier.VOLATILE)){
      appendModifier(buffer, PsiModifier.VOLATILE);
    }
    if (buffer.length() > 0){
      buffer.setLength(buffer.length() - 1);
    }
    return buffer.toString();
  }

  private static void appendModifier(final StringBuilder buffer, final String modifier) {
    buffer.append(modifier);
    buffer.append(' ');
  }

  public static String formatReferenceList(PsiReferenceList list, int options){
    StringBuilder buffer = new StringBuilder();
    PsiJavaCodeReferenceElement[] refs = list.getReferenceElements();
    for(int i = 0; i < refs.length; i++) {
      PsiJavaCodeReferenceElement ref = refs[i];
      if (i > 0){
        buffer.append(", ");
      }
      buffer.append(formatReference(ref, options));
    }
    return buffer.toString();
  }

  public static String formatType(PsiType type, int options, PsiSubstitutor substitutor){
    type = substitutor.substitute(type);
    if ((options & SHOW_RAW_TYPE) != 0) {
      type = TypeConversionUtil.erasure(type);
    }
    if ((options & SHOW_FQ_CLASS_NAMES) != 0){
      return type.getInternalCanonicalText();
    }
    else{
      return type.getPresentableText();
    }
  }

  public static String formatReference(PsiJavaCodeReferenceElement ref, int options){
    if ((options & SHOW_FQ_CLASS_NAMES) != 0){
      return ref.getCanonicalText();
    }
    else{
      return ref.getText();
    }
  }

  @Nullable
  public static String getExternalName(PsiModifierListOwner owner) {
    return getExternalName(owner, true);
  }

  @Nullable
  public static String getExternalName(PsiModifierListOwner owner, final boolean showParamName) {
    final StringBuilder builder = StringBuilderSpinAllocator.alloc();
    try {
      if (owner instanceof PsiClass) {
        ClassUtil.formatClassName((PsiClass)owner, builder);
        return builder.toString();
      }
      final PsiClass psiClass = PsiTreeUtil.getParentOfType(owner, PsiClass.class, false);
      assert psiClass != null;
      ClassUtil.formatClassName(psiClass, builder);
      if (owner instanceof PsiMethod) {
        return builder.toString() + " " + formatMethod((PsiMethod)owner, PsiSubstitutor.EMPTY,
                                                       SHOW_NAME | SHOW_FQ_NAME | SHOW_TYPE | SHOW_PARAMETERS | SHOW_FQ_CLASS_NAMES,
                                                       showParamName ? (SHOW_NAME | SHOW_TYPE | SHOW_FQ_CLASS_NAMES) : (SHOW_TYPE | SHOW_FQ_CLASS_NAMES));
      }
      else if (owner instanceof PsiField) {
        return builder.toString() + " " + ((PsiField)owner).getName();
      }
      else if (owner instanceof PsiParameter) {
        final PsiElement declarationScope = ((PsiParameter)owner).getDeclarationScope();
        if (declarationScope instanceof PsiMethod) {
          final PsiMethod psiMethod = (PsiMethod)declarationScope;
          return builder.toString() + " " + formatMethod(psiMethod, PsiSubstitutor.EMPTY,
                                                         SHOW_NAME | SHOW_FQ_NAME | SHOW_TYPE | SHOW_PARAMETERS | SHOW_FQ_CLASS_NAMES,
                                                         showParamName ? (SHOW_NAME | SHOW_TYPE | SHOW_FQ_CLASS_NAMES) : (SHOW_TYPE | SHOW_FQ_CLASS_NAMES)) + " " +
                 (showParamName ? formatVariable((PsiVariable)owner, SHOW_NAME, PsiSubstitutor.EMPTY) : psiMethod.getParameterList().getParameterIndex((PsiParameter)owner));
        }
      }
    }
    finally {
      StringBuilderSpinAllocator.dispose(builder);
    }
    return null;
  }

  public static String getPackageDisplayName(@NotNull final PsiClass psiClass) {
    @NonNls String packageName = psiClass.getQualifiedName();
    if (packageName != null && packageName.lastIndexOf('.') > 0) {
      packageName = packageName.substring(0, packageName.lastIndexOf('.'));
    }
    else {
      packageName = "";
    }
    if (packageName.length() == 0) {
      packageName = "default package";
    }
    return packageName;
  }
}
