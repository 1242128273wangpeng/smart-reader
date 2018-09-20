package net.lzbook.kit.bean;

/**
 * 阅读枚举
 * Created by wt on 2017/12/12.
 */

public interface ReadViewEnums {
    //ViewPager tag 枚举
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

    //阅读View滑动方向枚举
     enum Direction {
        leftToRight(true), rightToLeft(true), up(false), down(false);

        public final boolean IsHorizontal;

        Direction(boolean isHorizontal) {
            IsHorizontal = isHorizontal;
        }
    }
    //页面状态
    enum ViewState{
        loading(0),success(0),error(0),start(0),end(0),other(0);
        public int Tag = 0;
        ViewState(int tag) {
            Tag = tag;
        }
    }    //页面状态
    enum NotifyStateState{
        all,left,right,none
    }
    //动画类型
     enum Animation {
        //滑动覆盖
        slide, shift, list, curl,
    }

    //MsgType
     enum MsgType {
        MSG_LOAD_CUR_CHAPTER(0),
        MSG_LOAD_PRE_CHAPTER(1),
        MSG_LOAD_NEXT_CHAPTER(2),
        MSG_LOAD_JUMP_CHAPTER(3);

        public int Msg = -1;
        MsgType(int msg) {
            Msg = msg;
        }
    }

    enum ScrollLimitOrientation{
        NONE, LEFT, RIGHT, BOTH
    }
}
