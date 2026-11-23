package com.umc.movie.config;

/** Swagger에 표시할 예시입니다. 실제 ID와 시각은 저장 결과에 따라 달라집니다. */
public final class ApiExamples {
    private ApiExamples() {}

    public static final String FAVORITE_CREATED = """
            {"isSuccess":true,"code":"COMMON201","message":"즐겨찾기를 등록했습니다.",
             "result":{"favoriteId":1,"movieId":550,"createdAt":"2026-11-16T12:00:00"}}
            """;
    public static final String FAVORITE_LIST = """
            {"isSuccess":true,"code":"COMMON200","message":"내 즐겨찾기 목록을 조회했습니다.",
             "result":[{"favoriteId":2,"movieId":680,"createdAt":"2026-11-16T12:01:00"},
                       {"favoriteId":1,"movieId":550,"createdAt":"2026-11-16T12:00:00"}]}
            """;
    public static final String FAVORITE_TRUE = """
            {"isSuccess":true,"code":"COMMON200","message":"즐겨찾기 여부를 조회했습니다.","result":{"isFavorite":true}}
            """;
    public static final String FAVORITE_FALSE = """
            {"isSuccess":true,"code":"COMMON200","message":"즐겨찾기 여부를 조회했습니다.","result":{"isFavorite":false}}
            """;
    public static final String FAVORITE_DELETED = """
            {"isSuccess":true,"code":"COMMON200","message":"즐겨찾기를 해제했습니다.","result":null}
            """;
    public static final String FAVORITE_CONFLICT = """
            {"isSuccess":false,"code":"FAVORITE409_1","message":"이미 이 영화를 즐겨찾기하셨습니다.","result":null}
            """;
    public static final String FAVORITE_NOT_FOUND = """
            {"isSuccess":false,"code":"FAVORITE404_1","message":"즐겨찾기한 영화를 찾을 수 없습니다.","result":null}
            """;
    public static final String MEMBER_NOT_FOUND = """
            {"isSuccess":false,"code":"MEMBER404_1","message":"해당 회원을 찾을 수 없습니다.","result":null}
            """;
    public static final String VALIDATION_ERROR = """
            {"isSuccess":false,"code":"COMMON4001","message":"영화 ID는 필수입니다.","result":null}
            """;
    public static final String UNAUTHORIZED = """
            {"isSuccess":false,"code":"AUTH401_2","message":"로그인이 필요한 요청입니다.","result":null}
            """;
    public static final String INVALID_TOKEN = """
            {"isSuccess":false,"code":"AUTH401_3","message":"유효하지 않은 토큰입니다.","result":null}
            """;
    public static final String EXPIRED_TOKEN = """
            {"isSuccess":false,"code":"AUTH401_4","message":"토큰이 만료되었습니다. 다시 로그인해주세요.","result":null}
            """;
    public static final String MOVIE_RECORD_CREATED = """
            {"isSuccess":true,"code":"COMMON201","message":"영화 기록을 저장했습니다.",
             "result":{"rating":{"ratingId":1,"movieId":680,"score":5,"comment":"다시 보고 싶은 영화",
                                  "createdAt":"2026-11-16T12:00:00","updatedAt":"2026-11-16T12:00:00"},
                       "favorite":{"favoriteId":2,"movieId":680,"createdAt":"2026-11-16T12:00:00"}}}
            """;
    public static final String MOVIE_RECORD_WITHOUT_FAVORITE = """
            {"isSuccess":true,"code":"COMMON201","message":"영화 기록을 저장했습니다.",
             "result":{"rating":{"ratingId":1,"movieId":680,"score":5,"comment":null,
                                  "createdAt":"2026-11-16T12:00:00","updatedAt":"2026-11-16T12:00:00"},
                       "favorite":null}}
            """;
    public static final String RATING_CONFLICT = """
            {"isSuccess":false,"code":"RATING409_1","message":"이미 이 영화에 평점을 남기셨습니다.","result":null}
            """;
    public static final String RATING_FORBIDDEN = """
            {"isSuccess":false,"code":"RATING403_1","message":"본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.","result":null}
            """;
    public static final String RATING_NOT_FOUND = """
            {"isSuccess":false,"code":"RATING404_1","message":"해당 평점을 찾을 수 없습니다.","result":null}
            """;
    public static final String RATING_VALIDATION_ERROR = """
            {"isSuccess":false,"code":"COMMON4001","message":"별점은 1점 이상이어야 합니다.","result":null}
            """;
    public static final String INVALID_PAGE = """
            {"isSuccess":false,"code":"COMMON400","message":"page는 0 이상, size는 1~100이어야 합니다.","result":null}
            """;
    public static final String NICKNAME_CONFLICT = """
            {"isSuccess":false,"code":"MEMBER409_1","message":"이미 사용 중인 닉네임입니다.","result":null}
            """;
}
