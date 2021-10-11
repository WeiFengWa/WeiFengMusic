package com.wei.music.bean;
import java.util.ArrayList;


//歌单数据

public class UserMusicListBean {
    
    public playlist playlist;
    
    public class playlist {
        
        public String coverImgUrl;//歌单封面
        public String name;//歌单名
        public String description;//歌单介绍
        
        public ArrayList<tracks> tracks = new ArrayList();
        
        public class tracks {
            public String name;//歌曲名
            public String id;//歌曲ID
            
            public ArrayList<ar> ar = new ArrayList();
            
            public class ar {
                public String id;//歌手ID
                public String name;//歌手名
            }
            
            public al al;
            
            public class al {
                public String picUrl;//歌曲封面
            }
            
        }
    }
    
}
