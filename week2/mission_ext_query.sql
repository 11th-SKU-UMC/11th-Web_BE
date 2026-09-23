/*
“특정 회원이 진행 중인 미션ID, 보상 포인트와 해당 가게 이름을 조회한다. 
보상 포인트가 높은 순으로 조회한다.”
*/

SELECT m.mission_id,
       m.reward_point,
       s.name AS store_name
FROM member_mission mm
JOIN mission m ON mm.mission_id = m.mission_id
JOIN store s ON m.store_id = s.store_id
WHERE mm.member_id = 1
  AND mm.status = 'IN_PROGRESS'
ORDER BY m.reward_point DESC;

/*
특정 회원이 진행 중인 미션의 미션 ID, 보상 포인트와 가게 이름을 보상 포인트가 높은 순으로 조회하는 요구사항입니다. 
회원별 미션 진행 상태를 확인해야 하므로 `member_mission` 테이블을 기준으로 시작했습니다.

`member_mission`은 `mission_id`를 통해 `mission`과 연결되어 있어 미션 ID와 보상 포인트를 가져오기 위해 JOIN했습니다. 
또한 `mission`은 `store_id`를 통해 `store`와 연결되어 있어 해당 미션의 가게 이름을 가져오기 위해 JOIN했습니다.

먼저 `member_id = 1`과 `status = 'IN_PROGRESS'` 조건이 올바른지 확인했습니다. 
이후 `member_mission → mission → store`의 JOIN 관계가 제대로 연결되어 있는지와 `reward_point`가 내림차순으로 정렬되는지 확인했습니다.
*/

/*
실행 결과, 특정 회원이 진행 중인 미션의 미션 ID, 보상 포인트와 가게 이름이 조회되었으며, 보상 포인트가 높은 순으로 정렬된 것을 확인했습니다.
*/