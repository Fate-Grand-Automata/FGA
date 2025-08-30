# Contributing Guide

We welcome contributions to Fate/Grand Automata! This guide will help you get started with contributing to the project.

## Ways to Contribute

### 1. Reporting Issues
- Report bugs and issues on [GitHub Issues](https://github.com/Fate-Grand-Automata/FGA/issues)
- Provide detailed information including device model, Android version, and steps to reproduce
- Include screenshots or screen recordings when relevant

### 2. Documentation
- Improve existing documentation
- Add missing guides or tutorials
- Fix typos and clarify unclear sections
- Translate documentation to other languages

### 3. Code Contributions
- Fix bugs and implement new features
- Improve performance and optimization
- Enhance image recognition algorithms
- Add support for new game features

### 4. Testing
- Test new releases and provide feedback
- Test on different devices and Android versions
- Verify fixes for reported issues

### 5. Community Support
- Help other users in Discord
- Share knowledge and tips
- Create tutorials and guides

## Development Setup

### Prerequisites
- **Android Studio**: Latest stable version
- **JDK**: Java Development Kit 11 or higher
- **Git**: For version control
- **Android SDK**: API level 26 or higher

### Setting Up the Project

1. **Fork the repository** on GitHub
2. **Clone your fork**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/FGA.git
   cd FGA
   ```
3. **Open in Android Studio**
4. **Build the project** to ensure everything works

### Project Structure
```
FGA/
├── app/                    # Main application module
├── libautomata/           # Core automation library
├── prefs/                 # Preferences module
├── scripts/               # Build and automation scripts
├── docs/                  # Documentation (this site)
└── wiki/                  # Legacy wiki content
```

## Making Changes

### Branch Naming
Use descriptive branch names:
- `feature/new-feature-name`
- `bugfix/fix-description`
- `docs/documentation-update`

### Commit Messages
Write clear, descriptive commit messages:
```
Add support for new servant skill animations

- Implement detection for Kukulkan's special skills
- Add timing adjustments for transformation animations
- Update pattern recognition for new UI elements
```

### Code Style
- Follow existing code conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions focused and small

## Testing Your Changes

### Local Testing
1. **Build and install** your changes on a test device
2. **Test thoroughly** with different scenarios
3. **Verify** existing functionality still works
4. **Check** for memory leaks or performance issues

### Testing Guidelines
- Test on multiple Android versions when possible
- Test with different screen resolutions
- Verify changes work with various game content
- Test edge cases and error conditions

## Submitting Changes

### Pull Request Process

1. **Create a pull request** from your feature branch
2. **Describe your changes** clearly in the PR description
3. **Reference related issues** using GitHub keywords (e.g., "Fixes #123")
4. **Wait for review** from maintainers
5. **Address feedback** and make requested changes

### PR Description Template
```markdown
## Description
Brief description of what this PR does.

## Changes Made
- List of specific changes
- Another change
- etc.

## Testing
- How you tested these changes
- What scenarios were covered
- Any limitations or known issues

## Related Issues
Fixes #123
Related to #456
```

## Code Review Process

### For Contributors
- Be responsive to feedback
- Ask questions if feedback is unclear
- Make requested changes promptly
- Be patient during the review process

### Review Criteria
- **Functionality**: Does the code work as intended?
- **Quality**: Is the code clean and maintainable?
- **Performance**: Are there any performance implications?
- **Compatibility**: Does it work across different devices/versions?
- **Testing**: Has it been adequately tested?

## Documentation Contributions

### Documentation Standards
- Use clear, concise language
- Include code examples where relevant
- Add screenshots for UI-related documentation
- Keep content up to date with current app version

### Building Documentation Locally
```bash
# Install dependencies
pip install -r requirements.txt

# Serve documentation locally
mkdocs serve

# Build static documentation
mkdocs build
```

## Getting Help

### Development Questions
- Join our [Discord](https://discord.gg/fate-grand-automata) #development channel
- Ask questions in GitHub Discussions
- Reference existing code and documentation

### Learning Resources
- **Android Development**: [Android Developer Guides](https://developer.android.com/guide)
- **OpenCV**: [OpenCV Documentation](https://docs.opencv.org/)
- **Kotlin**: [Kotlin Documentation](https://kotlinlang.org/docs/)

## Community Guidelines

### Code of Conduct
- Be respectful and inclusive
- Help others learn and grow
- Give constructive feedback
- Follow project guidelines and standards

### Communication
- Use English for all project communication
- Be patient with contributors of all skill levels
- Provide helpful and detailed feedback
- Ask questions when something is unclear

## Recognition

Contributors are recognized in several ways:
- GitHub contributor graphs
- Release notes mention significant contributions
- Community recognition in Discord
- Potential maintainer invitation for long-term contributors

## Legal Considerations

### License
By contributing to FGA, you agree that your contributions will be licensed under the same license as the project.

### Originality
- Only submit original work or properly attributed content
- Don't include copyrighted material without permission
- Respect third-party licenses and attributions

## Project Roadmap

Check our [Improvements and Recommendations](developer/improvements-and-recommendations.md) for:
- Current improvement priorities
- Planned features and enhancements
- Areas where contributions are most needed
- Technical debt that needs addressing

Thank you for contributing to Fate/Grand Automata! Your contributions help make the app better for the entire community.