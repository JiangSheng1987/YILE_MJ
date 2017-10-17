package game.gateway;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.isnowfox.core.io.Input;
import com.isnowfox.core.io.MarkCompressInput;
import com.isnowfox.core.io.ProtocolException;
import com.isnowfox.core.net.Session;
import com.isnowfox.core.net.message.Packet;
import com.isnowfox.game.platform.User;
import com.isnowfox.game.proxy.message.AllPxMsg;
import com.isnowfox.game.proxy.message.LogoutPxMsg;
import com.isnowfox.game.proxy.message.PxMsg;
import com.isnowfox.game.proxy.message.RangePxMsg;
import com.isnowfox.game.proxy.message.SinglePxMsg;
import com.isnowfox.util.collect.primitive.ShortList;

import game.boss.ZJH.msg.DelZJHRoomMsg;
import game.boss.ZJH.msg.JoinZJHRoomMsg;
import game.boss.douniu.msg.DeDouniuRoomMsg;
import game.boss.douniu.msg.DouniuExitRoomMsg;
import game.boss.douniu.msg.JoinDouniuRoomMsg;
import game.boss.msg.DelRoomMsg;
import game.boss.msg.ExitRoomMsg;
import game.boss.msg.JoinRoomMsg;
import game.boss.msg.RegGatewayMsg;
import game.boss.msg.RegSessionMsg;
import game.gateway.server.BossClient;
import game.gateway.server.DouniuSceneClient;
import game.gateway.server.DouniuSceneClientManager;
import game.gateway.server.DouniuSceneInfo;
import game.gateway.server.SceneClient;
import game.gateway.server.SceneClientManager;
import game.gateway.server.SceneInfo;
import game.gateway.server.ZJHSceneClient;
import game.gateway.server.ZJHSceneClientManager;
import game.gateway.server.ZJHSceneInfo;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import mj.net.message.login.Login;
import mj.net.message.login.Ping;
import mj.net.message.login.douniu.CreateDouniuRoom;
import mj.net.message.login.douniu.DeDouniuRoom;
import mj.net.message.login.douniu.DouniuRoomHistoryList;
import mj.net.message.login.douniu.ExitDouniuRoom;
import mj.net.message.login.douniu.JoinDouniuRoom;
import mj.net.message.login.zjh.CreateZJHRoom;
import mj.net.message.login.zjh.DelZJHRoom;
import mj.net.message.login.zjh.JoinZJHRoom;
import mj.net.message.room.pdk.CreatePdkRoom;
import mj.net.message.room.pdk.JoinPdkRoom;

/**
 * @author zuoge85@gmail.com on 16/9/26.
 */
public class GatewayService {
	private final static Logger log = LoggerFactory
			.getLogger(GatewayService.class);

	@Autowired
	private SessionService sessionService;

	@Autowired
	private BossClient bossClient;

	@Autowired
	private SceneClientManager sceneClientManager;

	@Autowired
	private ZJHSceneClientManager ZJHSceneClientManager;

	@Autowired
	private DouniuSceneClientManager douniuSceneClientManger;

	private short gatewayId;

	private boolean isWebSocket = true;

	private ConcurrentHashMap<Short, SceneInfo> map = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Short, ZJHSceneInfo> ZJHmap = new ConcurrentHashMap<>();

	/*
	 * 斗牛
	 */
	private ConcurrentHashMap<Short, DouniuSceneInfo> douniuMap = new ConcurrentHashMap<>();

	private void broadcastToAll(Packet msg) {
		int m = sessionService.getMax();
		for (int i = 0; i < m; i++) {
			User u = sessionService.getUser(i);
			if (u != null) {
				msg.getBuf().retain();
				u.writeAndFlush(new BinaryWebSocketFrame(msg.getBuf()));
			}
		}
	}

	private void sendToUsers(ShortList list, Packet msg) {
		int len = list.size();
		for (int i = 0; i < len; i++) {
			User u = sessionService.getUser(list.get(i));
			if (u != null) {
				msg.getBuf().retain();
				u.writeAndFlush(new BinaryWebSocketFrame(msg.getBuf()));
			}
		}
	}

	private void sendToUser(int sessionId, Packet msg) {
		User u = sessionService.getUser(sessionId);
		if (u != null) {
			msg.retain();
			u.writeAndFlush(new BinaryWebSocketFrame(msg.getBuf()));
		}
	}

	/**
	 * 转发消息到 boss 服务器或者场景服务器
	 */
	private void forwardFormUser(Packet msg) throws IOException,
			ProtocolException {
		User u = msg.<User> getSession().get();
		msg.getBuf().retain();

		SinglePxMsg sm = new SinglePxMsg(u.getId(), msg.getBuf());
		msg.getBuf().readerIndex(msg.getBufOffset());
		ByteBufInputStream bin = new ByteBufInputStream(msg.getBuf(),msg.getLength());
		Input in = MarkCompressInput.create(bin);
		int type = in.readInt();
		int id = in.readInt();
		if(!(type == 7 && id == 14))
			log.debug("msg=[type=" + type + ",id=" + id + "]");

		if (type == 3) {
			if (id == CreateZJHRoom.ID || id == JoinZJHRoom.ID
					|| id == DelZJHRoom.ID) {
				bossClient.writeAndFlush(sm);
				log.info("转发到boss");
			} else {
				ZJHSceneInfo zjhSceneInfo = ZJHmap.get(u.getId());
				if (zjhSceneInfo != null) {
					ZJHSceneClientManager.forwardMessage(sm, zjhSceneInfo);
					return;
				} else {
					throw new RuntimeException("未分配场景");
				}
			}
		} else if (type == 7) {
			bossClient.writeAndFlush(sm);
		} else if (type == 1) {
			SceneInfo sceneInfo = map.get(u.getId());
			if (sceneInfo != null) {
				sceneClientManager.forwardMessage(sm, sceneInfo);
			} else {
				throw new RuntimeException("未分配场景");
			}
		} else if (type == 4) {
			if (id == CreatePdkRoom.ID || id == JoinPdkRoom.ID) {
				bossClient.writeAndFlush(sm);
			} else {
				SceneInfo sceneInfo = map.get(u.getId());
				if (sceneInfo != null) {
					sceneClientManager.forwardMessage(sm, sceneInfo);
				} else {
					throw new RuntimeException("未分配场景");
				}
			}

		} else if (type == 5) {
			if (id == CreateDouniuRoom.ID || id == JoinDouniuRoom.ID
					|| id == DeDouniuRoom.ID || id == ExitDouniuRoom.ID || id==DouniuRoomHistoryList.ID) {
				bossClient.writeAndFlush(sm);
				log.info("转发到boss");
			} else {
				DouniuSceneInfo douniuSceneInfo = douniuMap.get(u.getId());
				if (douniuSceneInfo != null) {
					douniuSceneClientManger.forwardMessage(sm, douniuSceneInfo);
					return;
				} else {
					throw new RuntimeException("未分配的场景");
				}
			}
		}
	}

	private void close(int sessionId) {
		User u = sessionService.getUser(sessionId);
		if (u != null) {
			if (u.getChannel().isActive()) {
				u.getChannel().close();
			} else {
				sessionService.unReg(u);
			}
		} else {
			LogoutPxMsg msg = new LogoutPxMsg((short) sessionId);
			bossClient.writeAndFlush(msg);
		}
	}

	void toBoss(PxMsg msg) {
		bossClient.writeAndFlush(msg);
	}

	/**
	 * 处理客户端过来的消息
	 */
	public void handlerClient(Packet msg) throws IOException, ProtocolException {
		forwardFormUser(msg);
	}

	/**
	 * 处理boss 过来的消息
	 */
	public void handlerBoss(PxMsg msg) throws Exception {
		int type = msg.getType();
		switch (msg.getType()) {
		case SinglePxMsg.ID: {
			SinglePxMsg sm = (SinglePxMsg) msg;

			Packet p = new Packet(sm.getBufLen(), (byte) 0, sm.getBuf(), 0);
//			log.debug("准备转发[单个]消息{} --> {}", sm, p);
			sendToUser(sm.getSessionId(), p);
			break;
		}
		case RangePxMsg.ID: {
			RangePxMsg sm = (RangePxMsg) msg;

			Packet p = new Packet(sm.getBufLen(), (byte) 0, sm.getBuf(), 0);
			ShortList userList = sm.getUserList();
			log.debug("准备转发[多个用户:{}]消息{} --> {}", userList, sm, p);
			sendToUsers(userList, p);
			break;
		}
		case AllPxMsg.ID: {
			AllPxMsg sm = (AllPxMsg) msg;

			Packet p = new Packet(sm.getBufLen(), (byte) 0, sm.getBuf(), 0);
			log.debug("准备转发[全部玩家]消息{} --> {}", sm, p);
			broadcastToAll(p);
			break;
		}
		case LogoutPxMsg.ID: {
			LogoutPxMsg sm = (LogoutPxMsg) msg;
			close(sm.getSessionId());
			break;
		}
		case JoinRoomMsg.ID: {
			// 开始处理玩家进入游戏的情况
			JoinRoomMsg sm = (JoinRoomMsg) msg;
			SceneInfo sceneInfo = new SceneInfo();
			sceneInfo.setScenePort(sm.getScenePort());
			sceneInfo.setSceneAddress(sm.getSceneAddress());
			sceneInfo.setSessionId(sm.getSessionId());
			sceneInfo.setSceneId(sm.getSceneId());
			map.put(sm.getSessionId(), sceneInfo);
			sceneClientManager.checkConnect(sm.getSceneId(),
					sm.getSceneAddress(), sm.getScenePort(), () -> {
						toBoss(sm);
					});
			break;
		}
		case ExitRoomMsg.ID: {
			// 开始处理玩家进入游戏的情况
			ExitRoomMsg sm = (ExitRoomMsg) msg;
			map.remove(sm.getSessionId());
			toBoss(sm);
			break;
		}
		case DelRoomMsg.ID: {
			// 开始处理玩家进入游戏的情况
			DelRoomMsg sm = (DelRoomMsg) msg;
			map.remove(sm.getSessionId());
			toBoss(sm);
			break;
		}
		case JoinZJHRoomMsg.ID: {
			JoinZJHRoomMsg ms = (JoinZJHRoomMsg) msg;
			ZJHSceneInfo info = new ZJHSceneInfo();
			info.setSceneAddress(ms.getZJHSceneAddress());
			info.setScenePort(ms.getZJHScenePort());
			info.setSessionId(ms.getSessionId());
			info.setZJHsceneId(ms.getZJHSceneId());
			ZJHmap.put(ms.getSessionId(), info);
			ZJHSceneClientManager.checkConnect(ms.getZJHSceneId(),
					ms.getZJHSceneAddress(), ms.getZJHScenePort(), () -> {
						toBoss(ms);
					});
			break;
		}
		case JoinDouniuRoomMsg.ID: { // 斗牛加入房间
			JoinDouniuRoomMsg ms = (JoinDouniuRoomMsg) msg;
			DouniuSceneInfo info = new DouniuSceneInfo();
			info.setSceneAddress(ms.getDouniuSceneAddress());
			info.setScenePort(ms.getDouniuScenePort());
			info.setSessionId(ms.getSessionId());
			info.setDouniuSceneId(ms.getDouniuSceneId());
			douniuMap.put(ms.getSessionId(), info);
			douniuSceneClientManger.checkConnect(ms.getDouniuSceneId(),
					ms.getDouniuSceneAddress(), ms.getDouniuScenePort(),
					() -> {
						toBoss(ms);
					});
			break;
		}
		case DeDouniuRoomMsg.ID: { // 斗牛删除房间
			DeDouniuRoomMsg dm = (DeDouniuRoomMsg) msg;
			douniuMap.remove(dm.getSessionId());
			toBoss(dm);
			break;
		}
		case DouniuExitRoomMsg.ID: {
			// 开始处理玩家退出游戏的情况
			DouniuExitRoomMsg sm = (DouniuExitRoomMsg) msg;
			douniuMap.remove(sm.getSessionId());
			toBoss(sm);
			break;
		}
		case DelZJHRoomMsg.ID: {
			DelZJHRoomMsg dm = (DelZJHRoomMsg) msg;
			map.remove(dm.getSessionId());
			toBoss(dm);
			break;
		}

		default:
			break;
		}
	}

	public void onBossConnect() {
		bossClient.writeAndFlush(new RegGatewayMsg(gatewayId));
	}

	public void onBossDisconnect() {
	}

	public void onSceneConnect(SceneClient client) {
		client.writeAndFlush(new RegGatewayMsg(gatewayId));
	}

	public void onSceneConnect(ZJHSceneClient client) {
		client.writeAndFlush(new RegGatewayMsg(gatewayId));
	}

	public void onSceneConnectDouniu(DouniuSceneClient client) {
		client.writeAndFlush(new RegGatewayMsg(gatewayId));
	}

	public void onSceneDisconnect(SceneClient client) {

	}

	public void onSceneDisconnect(ZJHSceneClient client) {

	}

	public void onSceneDisconnect(DouniuSceneClient client) {

	}

	public void handlerScene(PxMsg msg) throws Exception {
		handlerBoss(msg);
	}

	public void handlerZJHScene(PxMsg msg) throws Exception {
		handlerBoss(msg);
	}

	public void handlerDouniuScene(PxMsg msg) throws Exception {
		handlerBoss(msg);
	}

	public void onCreateSession(ChannelHandlerContext ctx, Session<User> session) {
		RegSessionMsg msg = new RegSessionMsg();
		msg.setSessionId(session.get().getId());

		InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel()
				.remoteAddress();
		String hostAddress = socketAddress.getAddress().getHostAddress();
		msg.setIp(hostAddress);
		bossClient.writeAndFlush(msg);
	}

	public boolean isWebSocket() {
		return isWebSocket;
	}

	public void setWebSocket(boolean webSocket) {
		isWebSocket = webSocket;
	}

	public void setGatewayId(short gatewayId) {
		this.gatewayId = gatewayId;
	}

}
