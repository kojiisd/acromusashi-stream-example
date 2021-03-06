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
package acromusashi.stream.example.bolt;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import org.apache.storm.task.TopologyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import acromusashi.stream.bolt.AmBaseBolt;
import acromusashi.stream.entity.StreamMessage;

/**
 * 共通メッセージ中のJSONから特定要素を抽出するBolt<br>
 *
 * @author kimura
 */
public class JsonExtractBolt extends AmBaseBolt
{
    /** serialVersionUID */
    private static final long        serialVersionUID = 4002032169715662295L;

    /** logger */
    private static final Logger      logger           = LoggerFactory.getLogger(
            JsonExtractBolt.class);

    /** 変換対象のエンティティクラス */
    protected String                 targetKey;

    /** Jsonツリー生成用のマッパー */
    protected transient ObjectMapper mapper;

    /**
     * JSONから抽出対象となるキー
     *
     * @param targetKey 抽出対象キー
     */
    public JsonExtractBolt(String targetKey)
    {
        this.targetKey = targetKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepare(@SuppressWarnings("rawtypes") Map arg0, TopologyContext arg1)
    {
        this.mapper = new ObjectMapper();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onExecute(StreamMessage message)
    {
        String jsonStr = message.getBody().toString();

        JsonNode rootJson;
        try
        {
            // メッセージ抽出対象をJsonNodeに変換する。
            rootJson = this.mapper.readTree(jsonStr);
        }
        catch (IOException ex)
        {
            String logFormat = "Recived message is not valid. Skip message. : Message={0}";
            logger.warn(MessageFormat.format(logFormat, jsonStr), ex);
            return;
        }

        // JsonNodeからキーに対応する要素を抽出する。
        JsonNode valueJson = rootJson.get(this.targetKey);
        if (valueJson == null)
        {
            String logFormat = "Target Value is not exist. : TargetKey={0}, Message={1}";
            logger.warn(MessageFormat.format(logFormat, this.targetKey, jsonStr));
            return;
        }

        StreamMessage sendMessage = new StreamMessage();
        sendMessage.setBody(valueJson.asText());
        emitWithOnlyAnchor(sendMessage);
    }
}
