# AI Code Assistant

![Build](https://github.com/BaronCyrus/ai_code_helper/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)


<!-- Plugin description -->
This plugin will automatically generate commit messages with AI.
This plugin can be your code assistant. you can chat on the right window.

<h3>Supported LLM Client:</h3>
<li>DeepSeek</li>
<li>OpenAI API</li>
<li>Gemini.</li>
<li>Grok</li>
<li>SiliconFlow(Mode Hub)</li>
<li>VolcanoEngine(Mode Hub)</li>

<h3>Usage for auto generate commit message:</h3>
<li>Select the code file you want to commit.</li>
<li>Click the "Generate AI Commit Message" button.</li>
<li>The generated commit message will be displayed in the commit message editor.</li>

<h3>Usage for auto diff code review:</h3>
<li>Select the code file you want to review by ai.</li>
<li>Click the "Generate AI Code Review" button.</li>
<li>The generated commit message will be displayed in the right tool window.</li>


<h3>Usage for auto code suggestion:</h3>
<li>Select the code you want to analyze.</li>
<li>Right click and chose a function (explain code or refactor code or find bugs).</li>
<li>The generated message will be displayed in the right tool window.</li>

<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "AI Code Assistant"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/BaronCyrus/ai_code_helper/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
