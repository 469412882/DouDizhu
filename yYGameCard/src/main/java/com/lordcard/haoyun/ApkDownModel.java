/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lordcard.haoyun;

import java.io.Serializable;
import java.util.Random;

/**
 */
public class ApkDownModel implements Serializable {
    private static final long serialVersionUID = 2072893447591548402L;

    public String name;
    public String id;
    public String url;
    public String iconUrl;
    public int priority;

    public ApkDownModel() {
        Random random = new Random();
        priority = random.nextInt(100);
    }
}
