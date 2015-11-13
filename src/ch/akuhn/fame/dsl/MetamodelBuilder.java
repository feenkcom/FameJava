package ch.akuhn.fame.dsl;

import java.util.HashMap;
import java.util.Map;

import ch.akuhn.fame.fm3.MetaDescription;
import ch.akuhn.fame.parser.DebugClient;
import ch.akuhn.fame.parser.ParseClient;

public class MetamodelBuilder {

    public interface Document {
        public Package beginPackage(String name);
        public void endDocument();
    }

    public interface Package {
        public Class beginClass(String name);
        public Document endPackage();
        public void endDocument();
        public Package beginPackage(String string);
    }

    public interface Class {
        public Class with(String name, String type, String opposite);
        public Class with(String name, String type);
        public Class withMany(String name, String type, String opposite);
        public Class withMany(String name, String type);
        public Package endClass();
        public Class beginClass(String string);
        public void endDocument();
        public Package beginPackage(String string);
    }

    private class DocumentImpl implements Document {

        public DocumentImpl() {
            client.beginDocument();
        }
        
        public Package beginPackage(String name) {
            return new PackageImpl(name);
        }
        
        public void endDocument() {
            client.endDocument();
        }
        
        private class PackageImpl implements Package {
        
            public final String packageName;

            public PackageImpl(String name) {
                assert name != null;
                client.beginElement("FM3.Package");
                client.beginAttribute("name");
                client.primitive(this.packageName = name);
                client.endAttribute("name");
                client.beginAttribute("classes");
            }

            public Class beginClass(String name) {
                return new ClassImpl(name);
            }

            public Document endPackage() {
                client.endAttribute("classes");
                client.endElement("FM3.Package");
                return DocumentImpl.this;
            }
        
            private class ClassImpl implements Class {
                
                public final String className;

                public ClassImpl(String name) {
                    assert name != null;
                    client.beginElement("FM3.Class");
                    client.serial(to(packageName + "." + name));
                    client.beginAttribute("name");
                    client.primitive(this.className = name);
                    client.endAttribute("name");
                    client.beginAttribute("attributes");
                }

                private Class with(String name, String type, String opposite, boolean multivalued) {
                    client.beginElement("FM3.Property");
                    client.serial(to(packageName + "." + className + "." + name));
                    client.beginAttribute("name");
                    client.primitive(name);
                    client.endAttribute("name");
                    client.beginAttribute("type");
                    this.typeOfProperty(type);
                    client.endAttribute("type");
                    if (opposite != null) {
                        client.beginAttribute("opposite");
                        client.reference(to(type + "." + opposite));
                        client.endAttribute("opposite");
                    }
                    if (multivalued) {
                        client.beginAttribute("multivalued");
                        client.primitive(true);
                        client.endAttribute("multivalued");
                    }
                    client.endElement("FM3.Property");
                    return this;
                }

                private void typeOfProperty(String type) {
                    if (MetaDescription.hasPrimitiveNamed(type))
                        client.reference(type);
                    else        
                        client.reference(to(type));
                }

                public Class with(String name, String type) {
                    return with(name, type, null, false);
                }

                public Class withMany(String name, String type, String opposite) {
                    return with(name, type, opposite, true);
                }

                public Class withMany(String name, String type) {
                    return with(name, type, null, true);
                }

                public Class with(String name, String type, String opposite) {
                    return with(name, type, opposite, false);
                }

                public Package endClass() {
                    client.endAttribute("attributes");
                    client.endElement("FM3.Class");
                    return PackageImpl.this;
                }

                public Class beginClass(String name) {
                    return endClass().beginClass(name);
                }

                public void endDocument() {
                    endClass().endDocument();
                }

                public Package beginPackage(String name) {
                    return endClass().endPackage().beginPackage(name);
                }
                
            }

            public void endDocument() {
                endPackage().endDocument();
            }

            public Package beginPackage(String name) {
                return endPackage().beginPackage(name);
            }
            
        }
        
    }
    
    private final ParseClient client;
    private Map<String,Integer> indexDict;
    
    public MetamodelBuilder(ParseClient client) {
        this.client = client;
        this.indexDict = new HashMap<String, Integer>();
    }
    
    public Document beginDocument() {
        return new DocumentImpl();
    }

    private int to(String name) {
        Integer key = indexDict.get(name);
        if (key != null) return key;
        indexDict.put(name, key = indexDict.size());
        return key;
    }
    
    public static void main(String[] args) {
        
        ParseClient pc = new DebugClient();
        
        new MetamodelBuilder(pc)

        .beginDocument()
            .beginPackage("RPG")
                .beginClass("Dragon")
                    .withMany("hoard", "RPG.Treasure", "keeper")
                    .withMany("killedBy", "RPG.Hero", "kills")
                .beginClass("Treasure")
                    .with("keeper", "RPG.Dragon", "hoard")
                    .with("owner", "RPG.Hero", "talisman")
                .beginClass("Hero")
                    .with("twin", "RPG.Hero", "twin")
                    .with("talisman", "RPG.Treasure", "owner")
                    .withMany("kills", "RPG.Dragon", "killedBy")
        .endDocument();    

//        m = new MetamodelBuilder(pc)
//
//        m.beginDocument(
//            m.beginPackage("RPG",
//                m.beginClass("Dragon",
//                    m.withMany("hoard", "RPG.Treasure", "keeper"),
//                    m.withMany("killedBy", "RPG.Hero", "kills")),
//                m.beginClass("Treasure",
//                    m.with("keeper", "RPG.Dragon", "hoard"),
//                    m.with("owner", "RPG.Hero", "talisman")),
//                m.beginClass("Hero",
//                    m.with("twin", "RPG.Hero", "twin"),
//                    m.with("talisman", "RPG.Treasure", "owner"),
//                    m.withMany("kills", "RPG.Dragon", "killedBy"))));
        
        System.out.println(pc);
        
    }
    
    
}

