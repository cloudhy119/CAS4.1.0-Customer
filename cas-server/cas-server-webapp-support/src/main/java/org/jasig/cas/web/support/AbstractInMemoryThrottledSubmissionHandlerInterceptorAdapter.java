/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.web.support;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of a HandlerInterceptorAdapter that keeps track of a mapping
 * of IP Addresses to number of failures to authenticate.
 * <p>
 * Note, this class relies on an external method for decrementing the counts
 * (i.e. a Quartz Job) and runs independent of the threshold of the parent.
 *
 * @author Scott Battaglia
 * @since 3.0.0.5
 */
public abstract class AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter
                extends AbstractThrottledSubmissionHandlerInterceptorAdapter {

    private static final double SUBMISSION_RATE_DIVIDEND = 1000.0;
    private final ConcurrentMap<String, Date> ipMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, List<Long>> failRecordMap = new ConcurrentHashMap<>();

    /**
     * change by huangyun 2018-08-15
     * 修改登录密码错误限制逻辑，逻辑改为：
     * 指定时间（failureRangeInSeconds）内错误次数不能超过指定次数（failureThreshold），否则禁止登录。登陆成功后错误次数记录清零
     * @param request the request
     * @return
     */
    @Override
    protected final boolean exceedsThreshold(final HttpServletRequest request) {
//        final Date last = this.ipMap.get(constructKey(request)); //上次密码错误的时间
//        if (last == null) {
//            return false;
//        }
        if(this.failRecordMap.get(constructKey(request)) == null) {
            return false;
        }
        int failCount = this.failRecordMap.get(constructKey(request)).size();
        return failCount >= getFailureThreshold();
//        return submissionRate(new Date(), last) > getThresholdRate();
    }

    /**
     * 登录成功调用。清除密码错误记录
     * @param request
     */
    @Override
    protected final void removeFailureRecord(final HttpServletRequest request) {
        this.failRecordMap.remove(constructKey(request));
        this.ipMap.remove(constructKey(request));
    }
    @Override
    protected final void recordSubmissionFailure(final HttpServletRequest request) {
//        this.ipMap.put(constructKey(request), new Date());

        //--- by huangyun 2018-08-15 ---
        long currentTimeMillis = System.currentTimeMillis();
        List<Long> failList = this.failRecordMap.get(constructKey(request));
        if(failList != null && !failList.isEmpty()) {
            this.failRecordMap.get(constructKey(request)).add(currentTimeMillis);
            //剔除不在时间限制范围内的错误记录
            List<Long> refreshFailRecordList = new ArrayList<>();
            for(long record : this.failRecordMap.get(constructKey(request))) {
                if((currentTimeMillis - record) / 1000 < getFailureRangeInSeconds()) {
                    refreshFailRecordList.add(record);
                }
            }
            this.failRecordMap.put(constructKey(request), refreshFailRecordList);
            if(failRecordMap.get(constructKey(request)).size() > getFailureThreshold()) {
                failRecordMap.get(constructKey(request)).remove(0);
            }
        } else {
            failList = new ArrayList<>();
            failList.add(currentTimeMillis);
            this.failRecordMap.put(constructKey(request), failList);
        }
    }

    /**
     * Construct key to be used by the throttling agent to track requests.
     *
     * @param request the request
     * @return the string
     */
    protected abstract String constructKey(HttpServletRequest request);

    /**
     * This class relies on an external configuration to clean it up. It ignores the threshold data in the parent class.
     */
    public final void decrementCounts() {
        final Set<Map.Entry<String, Date>> keys = this.ipMap.entrySet();
        logger.debug("Decrementing counts for throttler.  Starting key count: {}", keys.size());

        final Date now = new Date();
        for (final Iterator<Map.Entry<String, Date>> iter = keys.iterator(); iter.hasNext();) {
            final Map.Entry<String, Date> entry = iter.next();
            if (submissionRate(now, entry.getValue()) < getThresholdRate()) {
                logger.trace("Removing entry for key {}", entry.getKey());
                iter.remove();
            }
        }
        logger.debug("Done decrementing count for throttler.");
    }

    /**
     * Computes the instantaneous rate in between two given dates corresponding to two submissions.
     *
     * @param a First date.
     * @param b Second date.
     *
     * @return  Instantaneous submission rate in submissions/sec, e.g. <code>a - b</code>.
     */
    private double submissionRate(final Date a, final Date b) {
        return SUBMISSION_RATE_DIVIDEND / (a.getTime() - b.getTime());
    }
}
