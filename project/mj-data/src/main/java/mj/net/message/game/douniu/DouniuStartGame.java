package mj.net.message.game.douniu;

import java.io.IOException;

import com.isnowfox.core.io.Input;
import com.isnowfox.core.io.Output;
import com.isnowfox.core.io.ProtocolException;
import com.isnowfox.core.net.message.AbstractMessage;

/**
 * 
* @ClassName: PdkStartGame
* @Description: 斗牛开始游戏消息
* @author qixianghui
* @date 2017年7月10日
*
 */
public class DouniuStartGame extends AbstractMessage{
	public static final int TYPE = 5;
	public static final int ID = 8;
	
    public DouniuStartGame(){
		
	}
	@Override
	public void decode(Input arg0) throws IOException, ProtocolException {
		
	}
	
	@Override
	public void encode(Output arg0) throws IOException, ProtocolException {
		
	}
	
	@Override
	public String toString() {
		return "DouniuStartGame []";
	}
	@Override
	public int getMessageId() {
		return ID;
	}
	@Override
	public int getMessageType() {
		return TYPE;
	}
	
	
}
