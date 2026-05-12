<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
  "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
  "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
  <property name="charset" value="UTF-8"/>
  <property name="severity" value="warning"/>

  <module name="TreeWalker">
    <!-- Naming -->
    <module name="TypeName"/>
    <module name="MethodName"/>
    <module name="ConstantName"/>
    <module name="LocalVariableName"/>
    <module name="MemberName"/>
    <module name="PackageName"/>
    <module name="ParameterName"/>

    <!-- Imports -->
    <module name="AvoidStarImport"/>
    <module name="UnusedImports"/>
    <module name="RedundantImport"/>

    <!-- Size -->
    <module name="LineLength">
      <property name="max" value="150"/>
      <property name="ignorePattern" value="^package.*|^import.*|a]+ href|href|http://|https://|ftp://"/>
    </module>
    <module name="MethodLength">
      <property name="max" value="50"/>
    </module>
    <module name="ParameterNumber">
      <property name="max" value="7"/>
    </module>

    <!-- Whitespace -->
    <module name="WhitespaceAround"/>
    <module name="NoWhitespaceAfter"/>
    <module name="NoWhitespaceBefore"/>

    <!-- Blocks -->
    <module name="NeedBraces"/>
    <module name="LeftCurly"/>
    <module name="RightCurly"/>

    <!-- Coding -->
    <module name="EqualsHashCode"/>
    <module name="SimplifyBooleanExpression"/>
    <module name="SimplifyBooleanReturn"/>
    <module name="FinalClass"/>
    <module name="HideUtilityClassConstructor"/>
  </module>

  <module name="FileLength">
    <property name="max" value="500"/>
  </module>
  <module name="NewlineAtEndOfFile"/>
</module>
