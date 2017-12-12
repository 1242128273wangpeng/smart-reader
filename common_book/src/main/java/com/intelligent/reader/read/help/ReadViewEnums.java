package com.intelligent.reader.read.help;

/**
 * 阅读枚举
 * Created by wt on 2017/12/12.
 */

public interface ReadViewEnums {
    enum PageIndex {
        previous, current, next;
        public PageIndex getNext() {
            switch (this) {
                case previous:
                    return current;
                case current:
                    return next;
                default:
                    return null;
            }
        }
        public PageIndex getPrevious() {
            switch (this) {
                case next:
                    return current;
                case current:
                    return previous;
                default:
                    return null;
            }
        }
    }
}
