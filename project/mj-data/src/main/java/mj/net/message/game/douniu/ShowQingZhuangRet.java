package mj.net.message.game.douniu;

import java.io.IOException;
import com.isnowfox.core.io.Input;
import com.isnowfox.core.io.Output;
import com.isnowfox.core.io.ProtocolException;
import com.isnowfox.core.net.message.AbstractMessage;

public class ShowQingZhuangRet extends AbstractMessage {
	public static final int TYPE			 = 5;
	public static final int ID				 = 34;

	@Override
	public void decode(Input in) throws IOException, ProtocolException {
		// TODO Auto-generated method stub

	}

	@Override
	public void encode(Output out) throws IOException, ProtocolException {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return "ShowQingZhuangRet []";
	}

	@Override
	public int getMessageId() {
		// TODO Auto-generated method stub
		return ID;
	}

	@Override
	public int getMessageType() {
		// TODO Auto-generated method stub
		return TYPE;
	}

}
