# Booklist

A desktop application for managing your personal book collection. Built with Java Swing and designed for families who want to keep track of what they own, what they've lent out, and what they still want to read.

Pairs with a self-hosted [Web App](https://github.com/NeXoS355/booklist_webapp) for adding books on the go.

## Features

**Collection Management**
- Add books with author, title, series info and volume number
- Track borrowed books (lent to / borrowed from)
- Mark books as physical copy or ebook
- Add personal notes to any entry
- Rate books with 1-5 stars

**Discovery & Organization**
- Filter by author or series via the sidebar tree
- Full-text search across all fields
- View statistics: most popular authors, series, books per year
- Export your collection as CSV

**Automation**
- Fetch covers, descriptions and metadata via Google Books API
- Built-in update mechanism with SHA-256 verification

**Wishlist**
- Separate wishlist for books you want to buy
- Same author/title/series organization

**Sync**
- Connect to the companion [Web App](https://github.com/NeXoS355/booklist_webapp) to add books from any device
- Secure API token authentication

**Theming**
- Dark and light mode (FlatLaf)
- HiDPI / fractional scaling support
- German and English UI

## Getting Started

### Download

Grab the latest `booklist.jar` from the [Releases](https://github.com/NeXoS355/booklist/releases) page and run it:

```bash
java -jar booklist.jar
```

Requires JDK 21+.

### Build from Source

```bash
mvn clean package
java -jar target/booklist-3.4.1-jar-with-dependencies.jar
```

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| [FlatLaf](https://www.formdev.com/flatlaf/) | 3.7 | Modern Look & Feel |
| [Apache Derby](https://db.apache.org/derby/) | 10.17.1.0 | Embedded database |
| [Gson](https://github.com/google/gson) | 2.13.2 | JSON parsing |
| [OpenCSV](https://opencsv.sourceforge.net/) | 5.12.0 | CSV export |
| [ZXing](https://github.com/zxing/zxing) | 3.5.4 | QRCode generation |
| [Log4j](https://logging.apache.org/log4j/) | 2.25.3 | Logging |

## License

This is a personal project. Feel free to use it for inspiration.
