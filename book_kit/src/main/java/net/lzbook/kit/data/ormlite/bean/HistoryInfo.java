package net.lzbook.kit.data.ormlite.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="tb_history_info")
public class HistoryInfo {
	@DatabaseField(columnName="name")
	private String name;
	@DatabaseField(columnName="book_id", id = true)
	private String book_id;
	@DatabaseField(columnName="book_source_id")
	private String book_source_id;
	@DatabaseField(columnName="category")
	private String category;
	@DatabaseField(columnName="author")
	private String author;
	@DatabaseField(columnName="chapter_count")
	private int chapter_count;
	@DatabaseField(columnName="last_chapter_name")
	private String last_chapter_name;
	@DatabaseField(columnName="img_url")
	private String img_url;
	@DatabaseField(columnName="site")
	private String site;
	@DatabaseField(columnName="desc")
	private String desc;
	@DatabaseField(columnName="status")
	private int status;
	@DatabaseField(columnName="last_brow_time")
	private long last_brow_time;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBook_id() {
		return book_id;
	}

	public void setBook_id(String book_id) {
		this.book_id = book_id;
	}

	public String getBook_source_id() {
		return book_source_id;
	}

	public void setBook_source_id(String book_source_id) {
		this.book_source_id = book_source_id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getChapter_count() {
		return chapter_count;
	}

	public void setChapter_count(int chapter_count) {
		this.chapter_count = chapter_count;
	}

	public String getLast_chapter_name() {
		return last_chapter_name;
	}

	public void setLast_chapter_name(String last_chapter_name) {
		this.last_chapter_name = last_chapter_name;
	}

	public String getImg_url() {
		return img_url;
	}

	public void setImg_url(String img_url) {
		this.img_url = img_url;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getLast_brow_time() {
		return last_brow_time;
	}

	public void setLast_brow_time(long last_brow_time) {
		this.last_brow_time = last_brow_time;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
