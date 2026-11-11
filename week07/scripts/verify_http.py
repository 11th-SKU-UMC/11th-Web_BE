"""서버를 먼저 실행한 뒤 python scripts/verify_http.py 실행. Python 표준 라이브러리만 사용."""
import json, time, urllib.request, urllib.error
from pathlib import Path
BASE='http://127.0.0.1:8080'
opener=urllib.request.build_opener(urllib.request.ProxyHandler({}))
records=[]
def call(step,method,path,body=None,token=None,status=200,code='COMMON200'):
    headers={'Content-Type':'application/json'}
    if token: headers['Authorization']='Bearer '+token
    req=urllib.request.Request(BASE+path,data=json.dumps(body).encode() if body is not None else None,headers=headers,method=method)
    try: response=opener.open(req,timeout=10)
    except urllib.error.HTTPError as e: response=e
    data=json.load(response)
    record=dict(step=step,method=method,path=path,request=body,authorization='Bearer (로그인 토큰)' if token else '없음',status=response.code,response=data)
    records.append(record)
    assert response.code==status and data['code']==code, record
    print(step,response.code,code,'PASS')
    return data['result']
suffix=str(time.time_ns())[-7:]
for user,nickname in [('a','에이'),('b','비비')]:call('1'+user+' 회원가입','POST','/members',dict(email=user+suffix+'@umc.com',nickname=nickname+suffix,password='Password1!'),status=201,code='COMMON201')
a=call('2a 로그인','POST','/auth/login',dict(email='a'+suffix+'@umc.com',password='Password1!'))['accessToken']
b=call('2b 로그인','POST','/auth/login',dict(email='b'+suffix+'@umc.com',password='Password1!'))['accessToken']
r=call('3 본인 등록','POST','/ratings',dict(movieId=100,score=4.5,comment='재미있어요'),a,201,'COMMON201')['ratingId']
call('4 중복 등록','POST','/ratings',dict(movieId=100,score=4.5),a,409,'RATING409_1')
call('5 본인 수정','PATCH',f'/ratings/{r}',dict(score=5,comment='수정 완료'),a)
call('6 타인 수정','PATCH',f'/ratings/{r}',dict(score=1),b,403,'RATING403_1')
call('7 토큰 없이 조회','GET','/members/me',status=401,code='AUTH401_2')
call('8 없는 평점 삭제','DELETE','/ratings/99999',token=a,status=404,code='RATING404_1')
assert call('9 본인 삭제','DELETE',f'/ratings/{r}',token=a) is None
out=Path(__file__).resolve().parent.parent/'evidence'
out.mkdir(exist_ok=True)
(out/'http-results.json').write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding='utf-8')
