package com.cartoonizer.rag.migration;

/**
First Step: index the project files (different Splitters for each file type?):
- XML layout files
- `Activity` / `Fragment` classes
- custom `View` classes
- style/theme resources
- existing docs, migration notes, and examples
- Compose design system rules for your team
- Kotlin/Java classes that bind views
- navigation graphs
- adapter code for lists
- image loading / click handling code
- your team’s Compose patterns
- reusable composables
- theming rules
- state handling guidelines
- examples of View-to-Compose replacements

2nd. step: Splitter
Split documents by:
- one XML file
- one class
- one composable
- one migration note
Store metadata: Attach metadata such as:
- file path
- screen name
- module
- UI type (`xml`, `fragment`, `activity`, `compose`)

3rd. Step: Use “before and after” examples
RAG works especially well if you index:
- old View implementation
- new Compose implementation
- notes explaining the transformation


 * @author cagi
 */
public class MigrateViewsToCompose {
    
}
