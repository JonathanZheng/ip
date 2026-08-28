---
name: seedu-java-coding-standard
description: Apply the SE-EDU basic and intermediate Java coding standard when editing Java code in this project.
---

# SE-EDU Java coding standard

Apply these rules to all Java production and test code in this project. Use the
official standard for details: <https://se-education.org/guides/conventions/java/intermediate.html>.

## Naming

- Use lowercase package names.
- Use PascalCase nouns for classes and enums.
- Use camelCase nouns for variables and verbs for methods.
- Use SCREAMING_SNAKE_CASE for constants.
- Use names that are clear, English, and consistent with their scope.
- Name boolean variables and methods as predicates, such as `isDone` or `hasData`.
- Use plural names for collections and a common prefix for associated constants.
- Do not write acronyms in all capitals inside names.

## Layout and structure

- Use four spaces for indentation and K&R braces.
- Keep lines at or below 120 characters; wrap long lines with eight-space continuation indentation.
- Separate logical units with one blank line.
- Keep imports explicit and consistently ordered.
- Organize each class as documentation, declaration, static fields, instance fields, constructors,
  then methods.
- Put access modifiers first and use the standard modifier order.
- Attach array brackets to the type, initialize variables at declaration when practical, and keep
  variables in the smallest useful scope.
- Use `this` only when a field is shadowed by a parameter or local variable.
- Always use braces for loop and conditional bodies.

## Comments

- Write comments in clear American English.
- Add descriptive JavaDoc to every public class and public method, and to every non-trivial private
  method.
- Start JavaDoc with a short summary sentence and document parameters, return values, and exceptions
  when they add useful information.

Review this skill whenever Java code is added or changed, and preserve these conventions in the
resulting code.
