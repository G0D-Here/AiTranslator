# AI Translator App

An AI-powered **language translation app** that supports **real-time speech-to-text conversion**, **auto language detection**, and **text translation**. The app leverages machine learning models and natural language processing (NLP) techniques to provide seamless multilingual communication. With features like speech recognition, text-to-speech, and auto language detection, users can easily speak in one language and get translated text in another.

## Features

- **Speech-to-Text**: Converts user speech into text in real-time.
- **Auto Language Detection**: Automatically detects the language of the spoken text.
- **Text Translation**: Translates the detected text into the target language (e.g., Hindi, English).
- **Text-to-Speech**: Reads out the translated text in a chosen language.
- **Clipboard Functionality**: Copy translated text to the clipboard for easy use elsewhere.
- **Seamless UI**: A clean and intuitive UI built with Jetpack Compose for an enhanced user experience.

## Tech Stack

- **Kotlin**: Primary programming language.
- **Jetpack Compose**: For building a modern and responsive UI.
- **Firebase**: For real-time data storage and authentication.
- **ML Kit (Translation & Speech Recognition)**: For language translation and speech-to-text functionality.
- **NLP Models**: For language detection and text correction.
- **Text-to-Speech (TTS)**: For reading the translated text aloud.

## How it Works

1. **Speech-to-Text**: The app starts by listening to the user's speech when the mic button is pressed. The speech is then transcribed into text.
2. **Language Detection**: The app automatically detects the language of the transcribed text.
3. **Translation**: The detected text is then sent for translation (e.g., English to Hindi).
4. **Text-to-Speech**: After translation, the app reads the translated text aloud.
5. **Clipboard Functionality**: The user can copy the translated text with one click for use in any other app.

## Future Improvements

- **Add more languages** for translation.
- **Improve accuracy** of speech recognition by using advanced AI models.
- **Offline Support**: Allow offline functionality for translation and speech-to-text conversion.
- **Multi-language Translation**: Support for multiple languages in a single conversation.

## Contributing

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature-name`).
3. Commit your changes (`git commit -m 'Add some feature'`).
4. Push to the branch (`git push origin feature-name`).
5. Create a new Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
