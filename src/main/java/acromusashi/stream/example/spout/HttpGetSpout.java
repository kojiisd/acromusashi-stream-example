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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acromusashi.stream.entity.StreamMessage;
import acromusashi.stream.entity.StreamMessageHeader;
import acromusashi.stream.spout.AmConfigurationSpout;

/**
 * 一定間隔ごとにHTTPGetを行い、結果を取得して下流に送信するSpout
 *
 * @author kimura
 */
public class HttpGetSpout extends AmConfigurationSpout
{
    /** serialVersionUID */
    private static final long   serialVersionUID = -237111294339742815L;

    /** logger */
    private static final Logger logger           = LoggerFactory.getLogger(HttpGetSpout.class);

    /** デフォルトのインターバル */
    private static final long   DEFAULT_INTERVAL = 100;

    /** アクセス先URI */
    private String              targetUrl;

    /** HTTPGET */
    protected HttpGet           httpget;

    /** HttpClient */
    protected HttpClient        client;

    /** HTTPGetリクエストを送信するインターバル（ミリ秒） */
    protected long              interval         = DEFAULT_INTERVAL;

    /**
     * アクセス先URLを指定してインスタンスを生成する。
     *
     * @param targetUrl アクセス先URL
     */
    public HttpGetSpout(String targetUrl)
    {
        this.targetUrl = targetUrl;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void open(Map stormConf, TopologyContext context, SpoutOutputCollector collector)
    {
        super.open(stormConf, context, collector);
        this.httpget = new HttpGet(this.targetUrl);
        this.client = HttpClientBuilder.create().build();
    }

    /**
     * 一定間隔ごとにデータをBoltに送信する
     */
    @Override
    public void nextTuple()
    {
        String response = null;

        try
        {
            response = this.client.execute(this.httpget, new BasicResponseHandler());
        }
        catch (IOException ex)
        {
            String logFormat = "Http get failed. Skip target get. : TargetUrl={0}";
            logger.warn(MessageFormat.format(logFormat, this.targetUrl), ex);
            return;
        }

        StreamMessageHeader header = new StreamMessageHeader();
        header.setMessageId(UUID.randomUUID().toString());
        header.setTimestamp(System.currentTimeMillis());
        header.setType("http");

        StreamMessage message = new StreamMessage();
        message.setHeader(header);
        message.setBody(response);

        getCollector().emit(new Values(message));

        try
        {
            TimeUnit.MILLISECONDS.sleep(this.interval);
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

    /**
     * @param interval セットする interval
     */
    public void setInterval(long interval)
    {
        this.interval = interval;
    }
}
