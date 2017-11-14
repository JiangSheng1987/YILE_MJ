package game.scene.room;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Timer;

import org.springframework.beans.BeanUtils;

import com.github.davidmoten.grumpy.core.Position;

import game.scene.room.majiang.MajiangChapter;
import mj.data.UserPaiInfo;
import mj.net.message.game.GameRoomInfo;
import mj.net.message.game.GameUserInfo;
import mj.net.message.game.StaticsResultRet;

/**
 * @author zuoge85@gmail.com on 16/10/6.
 */
public class RoomInfo {
	
	public static final long Default_Del_Room_Time = 1000*60*3;
    private short sceneId;
    /**
     * 牌局id
     */
    private int roomId;
    /**
     * 创建用户id
     */
    private int createUserId;
    /**
     * 用户数
     */
    private int userMax;
    /**
     * 牌局uuid
     */
    private String uuid;

    /**
     * 房间的check-id,进入id,可以重复,但是不允许同时活跃状态的id 相同
     */
    
    
    private String roomCheckId;

    private boolean start;
    private boolean chapterStart;
    private int userCount = 0;

    private final SceneUser[] users;

    protected final MajiangChapter chapter;
	private int dingPaoCount = 0;
	private int state;
	private int userNum;
	private boolean ready[];
	private boolean isProxy;
	private boolean dingPaoChache[];//配合state==1用
	private boolean isfirstStart = true;
	
	private Timer voteDelRoomTimer;
	private int currentChapterNum;
	
    public RoomInfo(Room room,String rulesName,int userNum,int roomId) {
        chapter = new MajiangChapter(room, rulesName,userNum);
        users =  new SceneUser[userNum];
        this.roomId = roomId;
        voteDel= new boolean[userNum];
        this.ready = new boolean[userNum];
        dingPaoChache = new boolean[userNum];
        this.userNum = userNum;
        isProxy = room.getConfig().getInt("isProxy")==1;
        currentChapterNum = 0;
    }
  
    
    

    public int getCurrentChapterNum() {
		return currentChapterNum;
	}




	public void addCurrentChapterNum() {
		this.currentChapterNum++;
	}




	public Timer getVoteDelRoomTimer() {
		return voteDelRoomTimer;
	}




	public void setVoteDelRoomTimer(Timer voteDelRoomTimer) {
		this.voteDelRoomTimer = voteDelRoomTimer;
	}




	public int getUserNum() {
		return userNum;
	}
    
   

	public boolean isProxy() {
		return isProxy;
	}


	public void setProxy(boolean isProxy) {
		this.isProxy = isProxy;
	}


	public GameRoomInfo toMessage(int myLocationIndex) {
        GameRoomInfo g = new GameRoomInfo();
        g.setStart(start);

        g.setRoomCheckId(roomCheckId);
        g.setCreateUserId(createUserId);
        g.setLeftChapterNums(chapter.getLeftChapterNums());
        for (SceneUser u : users) {
            if (u != null) {
                GameUserInfo sceneUser = u.toMessage();
                excuteDistanceKm(sceneUser, u);
                g.addSceneUser(sceneUser);
            }
        }
        if (isStart()) {
//            if (chapter.isStart()) {
                g.setChapter(chapter.toMessage(myLocationIndex,state));
//            }
        }
        g.setState(state);
        return g;
    }
	
	

    
	public boolean[] getDingPaoChache() {
		return dingPaoChache;
	}


	public void clearDingPaoChache(){
		Arrays.fill(dingPaoChache, false);
	}
	
	public boolean isNeedShowPao(int index){
		return this.state==1 && (!dingPaoChache[index]);
	}

    public int getState() {
		return state;
	}


	public void setState(int state) {
		this.state = state;
	}


	public int getUserCount() {
		return userCount;
	}

    public void addUserCount(){
    	userCount++;
    }
	public void excuteDistanceKm(GameUserInfo msg, SceneUser curUser) {
        if (curUser.getLongitude() == 0 || curUser.getLatitude() == 0) {
            return;
        }
        for (int i = 0; i < users.length; i++) {
            SceneUser u = users[i];
            if (u != null && u.getLocationIndex() != msg.getLocationIndex()) {
                String distance = "";
                if (u.getLongitude() != 0 && u.getLatitude() != 0) {
                    distance = String.format(
                            "%.3f米",
                            distanceKm(curUser.getLatitude(), curUser.getLongitude(), u.getLatitude(), u.getLongitude())*1000
                    );
                } else {
                    distance = "位置未知";
                }
                Method writeMethod = BeanUtils.getPropertyDescriptor(GameUserInfo.class, "user" + i + "Distance").getWriteMethod();

                try {
                    writeMethod.invoke(msg, distance);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * @return 返回距离，单位千米！
     */
    public static double distanceKm(double latitude0, double longitude0, double latitude1, double longitude1) {
        return new Position(latitude0, longitude0).getDistanceToKm(new Position(latitude1, longitude1));
    }

    public void updateUserInfo(SceneUser sceneUser,boolean isFirst) {
        SceneUser oldUser = users[sceneUser.getLocationIndex()];
        if (oldUser == null) {
            users[sceneUser.getLocationIndex()] = sceneUser;
            int index = sceneUser.getLocationIndex();
            if(index!=0){
            	ready[index] = true;
            }
        } else {
            oldUser.setAvatar(sceneUser.getAvatar());
            oldUser.setLocationIndex(sceneUser.getLocationIndex());
            oldUser.setSex(sceneUser.getSex());
            oldUser.setUserId(sceneUser.getUserId());
        }
        if(isFirst){
        	sceneUser.setScore(0);
        }
        chapter.updateUser(sceneUser);
    }

    public SceneUser getUserInfo(int locationIndex) {
        return users[locationIndex];
    }


    public boolean isIsfirstStart() {
		return isfirstStart;
	}

	public void changeIsfirstStart() {
		this.isfirstStart = false;
	}

    public boolean removeUserInfo(int userId) {
        for (int i = 0; i < users.length; i++) {
            SceneUser u = users[i];
            if (u != null && u.getUserId() == userId) {
                users[i] = null;
                return true;
            }
        }
        return false;
    }

    public SceneUser[] getUsers() {
        return users;
    }
    //改变房间的积分
    public void changeScore(UserPaiInfo[] userPaiInfos) {
        for (int i = 0; i < users.length; i++) {
            SceneUser user = users[i];
            UserPaiInfo userPaiInfo = userPaiInfos[i];
            user.addScore(userPaiInfo.getScore());
           
        }
//        if (fangPaoIndex == -1) {
//            for (int i = 0; i < users.length; i++) {
//                if (i != huPaiLocationIndex) {
//                    users[i].removeScore(scoreNums);
//                }
//            }
//            users[huPaiLocationIndex].addScore(scoreNums * 3);
//        } else {
//            users[fangPaoIndex].removeScore(scoreNums);
//            users[huPaiLocationIndex].addScore(scoreNums);
//        }
    }

    public short getSceneId() {
        return sceneId;
    }

    public void setSceneId(short sceneId) {
        this.sceneId = sceneId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(int createUserId) {
        this.createUserId = createUserId;
    }

    public int getUserMax() {
        return userMax;
    }

    public void setUserMax(int userMax) {
        this.userMax = userMax;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public String getRoomCheckId() {
        return roomCheckId;
    }

    public void setRoomCheckId(String roomCheckId) {
        this.roomCheckId = roomCheckId;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public MajiangChapter getChapter() {
        return chapter;
    }

    @Override
    public String toString() {
        return "RoomInfo{" +
                "start=" + start +
                ", sceneId=" + sceneId +
                ", roomId=" + roomId +
                ", createUserId=" + createUserId +
                ", userMax=" + userMax +
                ", uuid='" + uuid + '\'' +
                ", roomCheckId='" + roomCheckId + '\'' +
                '}';
    }

    public boolean isFull() {
        for (int i = 0; i < users.length; i++) {
            SceneUser u = users[i];
            if (u == null || !u.isJoinGame()) {
                return false;
            }
        }
        return true;
    }

    public SceneUser getUserInfoByUserId(int userId) {
        for (int i = 0; i < users.length; i++) {
            SceneUser u = users[i];
            if (u != null && u.getUserId() == userId) {
                return u;
            }
        }
        return null;
    }

    public boolean isChapterStart() {
        return chapterStart;
    }

    public void setChapterStart(boolean chapterStart) {
        this.chapterStart = chapterStart;
    }

	public int getDingPaoCount() {
		// TODO 自动生成的方法存根
		return dingPaoCount ;
	}
	
	public void addDingPaoCount(){
		dingPaoCount++;
	}

	public void clearDingPao() {
		dingPaoCount=0;
	}

	private  boolean[] voteDel;
	public boolean voteDel(SceneUser user) {
		int count = 0;
		int onlineUserNum = 0;
		voteDel[user.getLocationIndex()] = true;
		for (int i = 0; i < voteDel.length; i++) {
			if(users[i]==null || !users[i].isOnline()){
				continue;
			}
			onlineUserNum++;
			if(voteDel[i] ){
				count++;
			}
			
		}
		if(count>=onlineUserNum-1)
			return true;
		return false;
	}


	public void clearVoteDel() {
		Arrays.fill(voteDel, false);
		
	}


	public void ready(SceneUser user) {
		ready[user.getLocationIndex()] = true;
		
	}


	public boolean allReady() {
		for (int i = 0; i < ready.length; i++) {
			if(!ready[i]){
				return false;
			}
		}
		return true;
	}


	public void clearReady() {
		for (int i = 0; i < ready.length; i++) {
			ready[i] = false;
		}
		
	}

	private StaticsResultRet endResult;
	public void setEndResult(StaticsResultRet staticsResultRet) {
		this.endResult = staticsResultRet;
	}


	public StaticsResultRet getEndResult() {
		return endResult;
	}




	public void exitRoom(int userId) {
		for (int i = 0; i < users.length; i++) {
			SceneUser user = users[i];
			if(user!=null && userId==user.getUserId()){
				users[i]=null;
				chapter.exitUser(userId);
			}
			
		}
		
	}
	
}
