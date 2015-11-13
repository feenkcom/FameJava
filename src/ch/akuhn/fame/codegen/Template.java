//  Copyright (c) 2007-2008 Adrian Kuhn <akuhn(a)iam.unibe.ch>
//  
//  This file is part of 'Fame (for Java)'.
//  
//  'Fame (for Java)' is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your
//  option) any later version.
//  
//  'Fame (for Java)' is distributed in the hope that it will be useful, but
//  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
//  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
//  License for more details.
//  
//  You should have received a copy of the GNU Lesser General Public License
//  along with 'Fame (for Java)'. If not, see <http://www.gnu.org/licenses/>.
//  

package ch.akuhn.fame.codegen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Template {

    public static Template get(String key) {
        try {
            Template template = getAllTemplates().get(key);
            assert template != null : key;
            return template;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Map<String, Template> getAllTemplates() throws IOException {
        InputStream input = ClassLoader.getSystemResourceAsStream("ch/akuhn/fame/codegen/template.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        Map<String, Template> map = new HashMap<String, Template>();
        String line = reader.readLine();
        while (true) {
            if (line == null)
                break;
            assert line.startsWith("%%%");
            StringBuilder template = new StringBuilder();
            String name = line.substring(4).trim();
            while (true) {
                line = reader.readLine();
                if (line == null || line.startsWith("%%%"))
                    break;
                template.append(line);
                template.append('\n');
            }
            map.put(name, new Template(name, template.toString()));
        }
        return map;
    }

    public static void main(String... argh) throws IOException {
        System.out.print(getAllTemplates());
    }

    private String template;
    private Map<String, String> values = new HashMap<String, String>();

    public Template(String name, String template) {
        this.template = template;
    }

    public String apply() {
        String result = template;
        for (String key : values.keySet()) {
            result = result.replace("--" + key + "--", values.get(key));
        }
        return result;
    }

    public void set(String key, String value) {
        values.put(key, value);
    }

    public void setAll(Template template) {
        values.putAll(template.values);
    }
}
