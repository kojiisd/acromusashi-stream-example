/**
* Copyright (c) Acroquest Technology Co, Ltd. All Rights Reserved.
* Please read the associated COPYRIGHTS file for more details.
*
* THE SOFTWARE IS PROVIDED BY Acroquest Technolog Co., Ltd.,
* WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
* BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDER BE LIABLE FOR ANY
* CLAIM, DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
* OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
*/
package acromusashi.stream.example.spout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acromusashi.stream.entity.StreamMessage;
import acromusashi.stream.entity.StreamMessageHeader;
import acromusashi.stream.spout.AmConfigurationSpout;

/**
 * 一定間隔ごとにSNMP形式の共通メッセージをBoltに送信するSpout。
 * 
 * @author tsukano
 */
public class PeriodicalSnmpGenSpout extends AmConfigurationSpout
{
    /** serialVersionUID */
    private static final long   serialVersionUID = -237111294339742815L;

    /** logger */
    private static final Logger logger           = LoggerFactory.getLogger(
            PeriodicalSnmpGenSpout.class);

    /** 送信カウンタ */
    private int                 counter          = 0;

    /**
     * パラメータを指定せずにインスタンスを生成する。
     */
    public PeriodicalSnmpGenSpout()
    {}

    /**
     * 一定間隔ごとにデータをBoltに送信する
     */
    @Override
    public void nextTuple()
    {
        this.counter++;
        StreamMessageHeader header = new StreamMessageHeader();
        header.setMessageId(UUID.randomUUID().toString());
        header.setTimestamp(System.currentTimeMillis());
        header.setSource("192.168.0.1");
        header.setType("snmp");
        header.addAdditionalHeader("SNMPVersion", "v2c");
        StreamMessage message = new StreamMessage();
        message.setHeader(header);

        List<Object> list = new ArrayList<Object>();
        list.add(
                "{\"sender\":\"localhost\",\"type\":\"snmp\",\"timestamp\":\"1345020868298\",\"version\":\"1.0\"}");
        list.add(this.counter);
        message.setBody(list);

        getCollector().emit(new Values(message));

        try
        {
            TimeUnit.SECONDS.sleep(1);
        }
        catch (InterruptedException iex)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Occur interrupt. Ignore interrupt.", iex);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("message"));
    }
}
