package com.dart.api.domain.gallery.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGallery is a Querydsl query type for Gallery
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGallery extends EntityPathBase<Gallery> {

    private static final long serialVersionUID = -568935369L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGallery gallery = new QGallery("gallery");

    public final com.dart.global.common.entity.QBaseTimeEntity _super = new com.dart.global.common.entity.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    public final EnumPath<Cost> cost = createEnum("cost", Cost.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> endDate = createDateTime("endDate", java.time.LocalDateTime.class);

    public final NumberPath<Integer> fee = createNumber("fee", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isPaid = createBoolean("isPaid");

    public final com.dart.api.domain.member.entity.QMember member;

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    public final StringPath thumbnail = createString("thumbnail");

    public final StringPath title = createString("title");

    public QGallery(String variable) {
        this(Gallery.class, forVariable(variable), INITS);
    }

    public QGallery(Path<? extends Gallery> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGallery(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGallery(PathMetadata metadata, PathInits inits) {
        this(Gallery.class, metadata, inits);
    }

    public QGallery(Class<? extends Gallery> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.dart.api.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

