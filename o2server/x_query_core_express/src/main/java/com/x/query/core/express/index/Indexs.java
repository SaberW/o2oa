package com.x.query.core.express.index;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.solr.store.hdfs.HdfsDirectory;

import com.x.base.core.project.bean.tuple.Triple;
import com.x.base.core.project.config.Config;
import com.x.base.core.project.config.Query;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.base.core.project.tools.ListTools;
import com.x.base.core.project.tools.NumberTools;

public class Indexs {

    private static final Logger LOGGER = LoggerFactory.getLogger(Indexs.class);

    public static final String CATEGORY_PROCESSPLATFORM = "processPlatform";
    public static final String CATEGORY_CMS = "cms";

    public static final String TYPE_WORKCOMPLETED = "workCompleted";
    public static final String TYPE_DOCUMENT = "document";

    public static final String FIELD_ID = "id";
    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_KEY = "key";
    public static final String FIELD_INDEXTIME = "indexTime";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_SUMMARY = "summary";
    public static final String FIELD_BODY = "body";
    public static final String FIELD_ATTACHMENT = "attachment";
    public static final String FIELD_CREATETIME = "createTime";
    public static final String FIELD_UPDATETIME = "updateTime";
    public static final String FIELD_CREATETIMEMONTH = "createTimeMonth";
    public static final String FIELD_UPDATETIMEMONTH = "updateTimeMonth";
    public static final String FIELD_READERS = "readers";
    public static final String FIELD_CREATORPERSON = "creatorPerson";
    public static final String FIELD_CREATORUNIT = "creatorUnit";
    public static final String FIELD_CREATORUNITLEVELNAME = "creatorUnitLevelName";
    public static final String FIELD_APPLICATION = "application";
    public static final String FIELD_APPLICATIONNAME = "applicationName";
    public static final String FIELD_APPLICATIONALIAS = "applicationAlias";
    public static final String FIELD_PROCESS = "processName";
    public static final String FIELD_PROCESSNAME = "processName";
    public static final String FIELD_PROCESSALIAS = "processAlias";
    public static final String FIELD_JOB = "job";
    public static final String FIELD_SERIAL = "serial";
    public static final String FIELD_EXPIRED = "expired";
    public static final String FIELD_EXPIRETIME = "expireTime";

    public static final String FIELD_APPID = "appId";
    public static final String FIELD_APPNAME = "appName";
    public static final String FIELD_APPALIAS = "appAlias";
    public static final String FIELD_CATEGORYID = "categoryId";
    public static final String FIELD_CATEGORYNAME = "categoryName";
    public static final String FIELD_CATEGORYALIAS = "categoryAlias";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_PUBLISHTIME = "publishTime";
    public static final String FIELD_MODIFYTIME = "modifyTime";

    public static final String FIELD_HIGHLIGHTING = "highlighting";
    public static final String READERS_SYMBOL_ALL = "ALL";

    public static final List<String> FACET_FIELDS = Stream.<String>of(FIELD_CATEGORY,
            FIELD_CREATETIMEMONTH, FIELD_UPDATETIMEMONTH, FIELD_APPLICATIONNAME,
            FIELD_PROCESSNAME, FIELD_APPNAME, FIELD_CATEGORYNAME,
            FIELD_CREATORPERSON, FIELD_CREATORUNIT)
            .collect(Collectors.toUnmodifiableList());

    private static final List<String> FIXED_DATE_FIELDS = Stream
            .<String>of(FIELD_INDEXTIME, FIELD_CREATETIME, FIELD_UPDATETIME)
            .collect(Collectors.toUnmodifiableList());

    public static final String FIELD_TYPE_STRING = "string";
    public static final String FIELD_TYPE_STRINGS = "strings";
    public static final String FIELD_TYPE_BOOLEAN = "boolean";
    public static final String FIELD_TYPE_BOOLEANS = "booleans";
    public static final String FIELD_TYPE_NUMBER = "number";
    public static final String FIELD_TYPE_NUMBERS = "numbers";
    public static final String FIELD_TYPE_DATE = "date";
    public static final String FIELD_TYPE_DATES = "dates";

    public static final String PREFIX_FIELD_DATA = "data_";
    public static final String PREFIX_FIELD_DATA_STRING = PREFIX_FIELD_DATA + FIELD_TYPE_STRING + "_";
    public static final String PREFIX_FIELD_DATA_STRINGS = PREFIX_FIELD_DATA + FIELD_TYPE_STRINGS + "_";
    public static final String PREFIX_FIELD_DATA_BOOLEAN = PREFIX_FIELD_DATA + FIELD_TYPE_BOOLEAN + "_";
    public static final String PREFIX_FIELD_DATA_BOOLEANS = PREFIX_FIELD_DATA + FIELD_TYPE_BOOLEANS + "_";
    public static final String PREFIX_FIELD_DATA_NUMBER = PREFIX_FIELD_DATA + FIELD_TYPE_NUMBER + "_";
    public static final String PREFIX_FIELD_DATA_NUMBERS = PREFIX_FIELD_DATA + FIELD_TYPE_NUMBERS + "_";
    public static final String PREFIX_FIELD_DATA_DATE = PREFIX_FIELD_DATA + FIELD_TYPE_DATE + "_";
    public static final String PREFIX_FIELD_DATA_DATES = PREFIX_FIELD_DATA + FIELD_TYPE_DATES + "_";

    private static final String[] QUERY_IGNORES = new String[] { "[", "]", "*", "?" };
    private static final String[] QUERY_IGNOREREPLACES = new String[] { "", "", "", "" };

    public static final String DIRECTORY_SEARCH = "search";

    public static final Integer DEFAULT_MAX_HITS = 1000000;

    public static String alignQuery(String query) {
        return StringUtils.replaceEach(query, QUERY_IGNORES, QUERY_IGNOREREPLACES);
    }

    public static Integer rows(Integer size) throws Exception {
        if (NumberTools.nullOrLessThan(size, 1)) {
            return Config.query().index().getSearchSize();
        } else {
            return (NumberTools.nullOrGreaterThan(size, Config.query().index().getSearchMaxSize())
                    ? Config.query().index().getSearchMaxSize()
                    : size);
        }
    }

    public static Integer start(Integer page, int rows) {
        return (NumberTools.nullOrLessThan(page, 1) ? 0 : page - 1) * rows;
    }

    public static boolean deleteDirectory(String category, String type, String key) {
        try {
            if (StringUtils.equals(Config.query().index().getMode(), Query.Index.MODE_HDFSDIRECTORY)) {
                org.apache.hadoop.conf.Configuration configuration = hdfsConfiguration();
                try (org.apache.hadoop.fs.FileSystem fileSystem = org.apache.hadoop.fs.FileSystem
                        .get(configuration)) {
                    org.apache.hadoop.fs.Path path = hdfsBase();
                    path = new org.apache.hadoop.fs.Path(path, category);
                    path = new org.apache.hadoop.fs.Path(path, type);
                    path = new org.apache.hadoop.fs.Path(path, key);
                    if (fileSystem.exists(path) && fileSystem.getFileStatus(path).isDirectory()) {
                        return fileSystem.delete(path, true);
                    }
                }
            } else {
                java.nio.file.Path path = Config.path_local_repository_index(true).resolve(category).resolve(type)
                        .resolve(key);
                if (Files.exists(path) && Files.isDirectory(path)) {
                    Files.walkFileTree(path, new SimpleFileVisitor<java.nio.file.Path>() {
                        @Override
                        public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException exc)
                                throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs)
                                throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static Optional<Directory> directory(String category, String type, String key, boolean checkExists) {
        try {
            if (StringUtils.equals(Config.query().index().getMode(), Query.Index.MODE_HDFSDIRECTORY)) {
                org.apache.hadoop.conf.Configuration configuration = hdfsConfiguration();
                org.apache.hadoop.fs.Path path = hdfsBase();
                path = new org.apache.hadoop.fs.Path(path, category);
                path = new org.apache.hadoop.fs.Path(path, type);
                path = new org.apache.hadoop.fs.Path(path, key);
                if (checkExists) {
                    try (org.apache.hadoop.fs.FileSystem fileSystem = org.apache.hadoop.fs.FileSystem
                            .get(configuration)) {
                        if (!fileSystem.exists(path)) {
                            return Optional.empty();
                        }
                    }
                }
                return Optional.of(new HdfsDirectory(path, configuration));
            } else {
                java.nio.file.Path path = Config.path_local_repository_index(true).resolve(category).resolve(type)
                        .resolve(key);
                if (checkExists && (!Files.exists(path))) {
                    return Optional.empty();
                }
                return Optional.of(FSDirectory.open(path));
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return Optional.empty();
    }

    @SuppressWarnings("deprecation")
    public static Optional<Directory> searchDirectory(boolean checkExists) {
        try {
            if (StringUtils.equals(Config.query().index().getMode(), Query.Index.MODE_HDFSDIRECTORY)) {
                org.apache.hadoop.conf.Configuration configuration = hdfsConfiguration();
                org.apache.hadoop.fs.Path path = hdfsBase();
                path = new org.apache.hadoop.fs.Path(path, DIRECTORY_SEARCH);
                if (checkExists) {
                    try (org.apache.hadoop.fs.FileSystem fileSystem = org.apache.hadoop.fs.FileSystem
                            .get(configuration)) {
                        if (!fileSystem.exists(path)) {
                            return Optional.empty();
                        }
                    }
                }
                return Optional.of(new HdfsDirectory(path, configuration));
            } else {
                java.nio.file.Path path = Config.path_local_repository_index(true).resolve(DIRECTORY_SEARCH);
                if (checkExists && (!Files.exists(path))) {
                    return Optional.empty();
                }
                return Optional.of(FSDirectory.open(path));
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return Optional.empty();
    }

    private static org.apache.hadoop.conf.Configuration hdfsConfiguration() throws Exception {
        org.apache.hadoop.conf.Configuration configuration = new org.apache.hadoop.conf.Configuration();
        configuration.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY,
                Config.query().index().getHdfsDirectoryDefaultFS());
        return configuration;
    }

    private static org.apache.hadoop.fs.Path hdfsBase() throws IllegalArgumentException, Exception {
        return new org.apache.hadoop.fs.Path(Config.query().index().getDirectoryPath());
    }

    public static List<String> subDirectoryPathOfCategoryType(String category, String type) {
        try {
            if (StringUtils.equals(Config.query().index().getMode(), Query.Index.MODE_HDFSDIRECTORY)) {
                return subDirectoryPathOfCategoryTypeHdfs(category, type);
            } else {
                java.nio.file.Path path = Config.path_local_repository_index(true).resolve(category).resolve(type);
                if (Files.exists(path)) {
                    List<String> list = new ArrayList<>();
                    try (Stream<java.nio.file.Path> stream = Files.walk(path, 1)) {
                        stream.filter(o -> {
                            try {
                                return !Files.isSameFile(o, path);
                            } catch (IOException e) {
                                LOGGER.error(e);
                            }
                            return false;
                        }).filter(Files::isDirectory).map(path::relativize).map(java.nio.file.Path::toString)
                                .forEach(list::add);
                    }
                    return list;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return new ArrayList<>();
    }

    private static List<String> subDirectoryPathOfCategoryTypeHdfs(String category, String type)
            throws Exception {
        List<String> list = new ArrayList<>();
        org.apache.hadoop.conf.Configuration configuration = hdfsConfiguration();
        try (org.apache.hadoop.fs.FileSystem fileSystem = org.apache.hadoop.fs.FileSystem.get(configuration)) {
            org.apache.hadoop.fs.Path path = hdfsBase();
            path = new org.apache.hadoop.fs.Path(path, category);
            path = new org.apache.hadoop.fs.Path(path, type);
            if (fileSystem.exists(path)) {
                RemoteIterator<LocatedFileStatus> fileStatusListIterator = fileSystem.listLocatedStatus(path);
                while (fileStatusListIterator.hasNext()) {
                    LocatedFileStatus locatedFileStatus = fileStatusListIterator.next();
                    if (locatedFileStatus.isDirectory()) {
                        list.add(locatedFileStatus.getPath().getName());
                    }
                }
            }
        }
        return list;
    }

    public static Optional<org.apache.lucene.search.Query> readersQuery(List<String> readers) {
        if (ListTools.isEmpty(readers)) {
            return Optional.empty();
        }
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        readers.stream().filter(StringUtils::isNotBlank).map(o -> new TermQuery(new Term(FIELD_READERS, o)))
                .forEach(o -> builder.add(o, BooleanClause.Occur.SHOULD));
        return Optional.of(builder.build());
    }

    public static List<org.apache.lucene.search.Query> filterQueries(List<Filter> filters) {
        List<org.apache.lucene.search.Query> list = new ArrayList<>();
        if (ListTools.isEmpty(filters)) {
            return list;
        }
        List<Filter> fields = filters.stream().filter(o -> FACET_FIELDS.contains(o.getField()))
                .collect(Collectors.toList());
        if (ListTools.isEmpty(fields)) {
            return list;
        }
        fields.stream().map(Indexs::fitlerQuery).filter(Optional::isPresent).forEach(o -> list.add(o.get()));
        return list;
    }

    private static Optional<org.apache.lucene.search.Query> fitlerQuery(Filter filter) {
        if (ListTools.isEmpty(filter.getValueList())) {
            return Optional.empty();
        }
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        filter.getValueList().stream().filter(StringUtils::isNotBlank)
                .map(o -> new TermQuery(new Term(filter.getField(), o)))
                .forEach(o -> builder.add(o, BooleanClause.Occur.SHOULD));
        return Optional.of(builder.build());
    }

    public static List<String> adjustFacetField(List<String> filters) {
        List<String> list = FACET_FIELDS.stream().filter(o -> (!filters.contains(o))).collect(Collectors.toList());
        if (list.contains(FIELD_PROCESSNAME)) {
            list.removeAll(Arrays.asList(FIELD_APPLICATIONNAME, FIELD_PROCESSNAME,
                    FIELD_APPNAME, FIELD_CATEGORYNAME));
        }
        if (list.contains(FIELD_APPLICATIONNAME)) {
            list.removeAll(Arrays.asList(FIELD_APPLICATIONNAME, FIELD_APPNAME,
                    FIELD_CATEGORYNAME));
        }
        if (list.contains(FIELD_CATEGORYNAME)) {
            list.removeAll(Arrays.asList(FIELD_APPNAME, FIELD_CATEGORYNAME,
                    FIELD_APPLICATIONNAME, FIELD_PROCESSNAME));
        }
        if (list.contains(FIELD_APPNAME)) {
            list.removeAll(Arrays.asList(FIELD_APPNAME, FIELD_APPLICATIONNAME,
                    FIELD_PROCESSNAME));
        }
        return list;
    }

    /**
     * 判断字段属性.
     * 
     * @param field
     * @return 名称,显示名称,类型
     */

    public static Triple<String, String, String> judgeField(String field) {
        if (StringUtils.startsWith(field, Indexs.PREFIX_FIELD_DATA_STRING)) {
            return Triple.of(field, StringUtils.substringAfter(field, Indexs.PREFIX_FIELD_DATA_STRING),
                    Indexs.FIELD_TYPE_STRING);
        } else if (StringUtils.startsWith(field, Indexs.PREFIX_FIELD_DATA_DATE)) {
            return Triple.of(field, StringUtils.substringAfter(field, Indexs.PREFIX_FIELD_DATA_DATE),
                    Indexs.FIELD_TYPE_DATE);
        } else if (StringUtils.startsWith(field, Indexs.PREFIX_FIELD_DATA_NUMBER)) {
            return Triple.of(field, StringUtils.substringAfter(field, Indexs.PREFIX_FIELD_DATA_NUMBER),
                    Indexs.FIELD_TYPE_NUMBER);
        } else if (StringUtils.startsWith(field, Indexs.PREFIX_FIELD_DATA_BOOLEAN)) {
            return Triple.of(field, StringUtils.substringAfter(field, Indexs.PREFIX_FIELD_DATA_BOOLEAN),
                    Indexs.FIELD_TYPE_BOOLEAN);
        } else if (StringUtils.startsWith(field, Indexs.PREFIX_FIELD_DATA_STRINGS)) {
            return Triple.of(field, StringUtils.substringAfter(field, Indexs.PREFIX_FIELD_DATA_STRINGS),
                    Indexs.FIELD_TYPE_STRINGS);
        } else if (StringUtils.startsWith(field, Indexs.PREFIX_FIELD_DATA_DATES)) {
            return Triple.of(field, StringUtils.substringAfter(field, Indexs.PREFIX_FIELD_DATA_DATES),
                    Indexs.FIELD_TYPE_DATES);
        } else if (StringUtils.startsWith(field, Indexs.PREFIX_FIELD_DATA_NUMBERS)) {
            return Triple.of(field, StringUtils.substringAfter(field, Indexs.PREFIX_FIELD_DATA_NUMBERS),
                    Indexs.FIELD_TYPE_NUMBERS);
        } else if (StringUtils.startsWith(field, Indexs.PREFIX_FIELD_DATA_BOOLEANS)) {
            return Triple.of(field, StringUtils.substringAfter(field, Indexs.PREFIX_FIELD_DATA_BOOLEANS),
                    Indexs.FIELD_TYPE_BOOLEANS);
        } else if (FIXED_DATE_FIELDS.contains(field)) {
            return Triple.of(field, field, Indexs.FIELD_TYPE_DATE);
        } else {
            return Triple.of(field, field, Indexs.FIELD_TYPE_STRING);
        }
    }
}
