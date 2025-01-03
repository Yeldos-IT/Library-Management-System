package library;

public class Books {
    private int id;
    private String title;
    private String author;
    private int year;
    private int pages;
    private String pdfPath;

    public Books(int id, String title, String author, int year, int pages, String pdfPath) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.pages = pages;
        this.pdfPath = pdfPath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
}