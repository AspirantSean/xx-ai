package com.dbapp.extension.sync.constant;

import java.util.function.Function;

public class Constant {

    public static String quotation = "\"";

    public static final String VERSION_VIEW_NAME = "incremental_version_view_";

    public static Function<String, String> syncVersionTableNameGetter = table -> "sync_version_" + table;

    public static Function<String, String> syncVersionRecordTableNameGetter = index -> "sync_version_record_" + index;

}
