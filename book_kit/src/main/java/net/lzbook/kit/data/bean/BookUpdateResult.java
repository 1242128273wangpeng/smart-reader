package net.lzbook.kit.data.bean;

import java.util.ArrayList;

public class BookUpdateResult {
	//小说更新结果
	public ArrayList<BookUpdate> items;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BookUpdateResult that = (BookUpdateResult) o;

		return items != null ? items.equals(that.items) : that.items == null;

	}

	@Override
	public int hashCode() {
		return items != null ? items.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "BookUpdateResult{" +
				"items=" + items +
				'}';
	}
}
