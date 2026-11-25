
package com.example.s2366848;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

// Parses Traffic Scotland XML data using XmlPullParser.
public class TrafficDataParser {

    public ArrayList<Situation> parse(String xml)
            throws XmlPullParserException, IOException {

        ArrayList<Situation> situations = new ArrayList<>();

        XmlPullParserFactory factory =
                XmlPullParserFactory.newInstance();

        factory.setNamespaceAware(true);

        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(xml));

        String type = "Unknown";
        String comment = "";
        String roadName = "Unknown";
        String latitude = "";
        String longitude = "";
        String startTime = "Unknown";
        String endTime = "Unknown";
        String delay = "Not specified";
        String validityStatus = "Unknown";

        boolean insideComment = false;
        boolean insideLocationName = false;

        int eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {

            if (eventType == XmlPullParser.START_TAG) {

                String tag = xpp.getName();

                if ("situation".equals(tag)) {
                    type = "Unknown";
                    comment = "";
                    roadName = "Unknown";
                    latitude = "";
                    longitude = "";
                    startTime = "Unknown";
                    endTime = "Unknown";
                    delay = "Not specified";
                    validityStatus = "Unknown";
                }

                if ("situationRecord".equals(tag)) {
                    String recordType = xpp.getAttributeValue(
                            "http://www.w3.org/2001/XMLSchema-instance",
                            "type"
                    );

                    if (recordType != null) {
                        type = recordType;
                    }
                }

                if ("generalPublicComment".equals(tag)) {
                    insideComment = true;
                }

                if ("name".equals(tag)
                        || "ilc".equals(tag)
                        || "otherName".equals(tag)) {
                    insideLocationName = true;
                }

                if ("value".equals(tag)) {
                    String value = xpp.nextText().trim();

                    if (insideComment) {
                        comment = comment.isEmpty()
                                ? value : comment + " " + value;

                    } else if (insideLocationName
                            && "Unknown".equals(roadName)) {
                        roadName = value;
                    }
                }

                if ("latitude".equals(tag)) {
                    String value = xpp.nextText().trim();

                    if (latitude.isEmpty()) {
                        latitude = value;
                    }
                }

                if ("longitude".equals(tag)) {
                    String value = xpp.nextText().trim();

                    if (longitude.isEmpty()) {
                        longitude = value;
                    }
                }

                if ("overallStartTime".equals(tag)) {
                    startTime = xpp.nextText().trim();
                }

                if ("overallEndTime".equals(tag)) {
                    endTime = xpp.nextText().trim();
                }

                if ("validityStatus".equals(tag)) {
                    validityStatus = xpp.nextText().trim();
                }

                if ("delaysType".equals(tag)) {
                    delay = xpp.nextText().trim();
                }

            } else if (eventType == XmlPullParser.END_TAG) {

                String tag = xpp.getName();

                if ("generalPublicComment".equals(tag)) {
                    insideComment = false;
                }

                if ("name".equals(tag)
                        || "ilc".equals(tag)
                        || "otherName".equals(tag)) {
                    insideLocationName = false;
                }

                if ("situation".equals(tag)) {

                    situations.add(new Situation(
                            type,
                            comment,
                            roadName,
                            latitude,
                            longitude,
                            startTime,
                            endTime,
                            delay,
                            validityStatus
                    ));
                }
            }

            eventType = xpp.next();
        }

        return situations;
    }
}
