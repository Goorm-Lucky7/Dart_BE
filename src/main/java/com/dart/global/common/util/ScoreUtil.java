package com.dart.global.common.util;

import static com.dart.global.common.util.GlobalConstant.*;

import com.dart.api.domain.review.entity.QReview;
import com.dart.api.domain.review.entity.Score;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;

public class ScoreUtil {
	public static NumberExpression<Double> getAverageScore(QReview review) {
		return new CaseBuilder()
			.when(review.score.eq(Score.ONE_STAR)).then(ONE_STAR)
			.when(review.score.eq(Score.TWO_STAR)).then(TWO_STAR)
			.when(review.score.eq(Score.THREE_STAR)).then(THREE_STAR)
			.when(review.score.eq(Score.FOUR_STAR)).then(FOUR_STAR)
			.when(review.score.eq(Score.FIVE_STAR)).then(FIVE_STAR)
			.otherwise(ZERO_STAR)
			.avg();
	}
}
