type MemberRole = "leader" | "member";

interface StudyMember {
  id: number;
  name: string;
  role: MemberRole;
  githubId?: string;
}

const members: StudyMember[] = [
  {
    id: 1,
    name: "현강",
    role: "leader",
    githubId: "hyeonkangkimm",
  },
  {
    id: 2,
    name: "홍길동",
    role: "member",
  },
];

function createMemberMessage(memberId: number) {
  const member = members.find((member) => member.id === memberId);

  if (!member) {
    return `ID ${memberId}: 존재하지 않는 회원입니다.`;
  }

  const roleMessage =
    member.role === "leader"
      ? "스터디를 이끌어요."
      : "스터디에 참여해요.";

  const githubId = member.githubId ?? "등록되지 않음";

  return `${member.name} 님은 ${roleMessage} GitHub ID: ${githubId}`;
}

console.log(createMemberMessage(1));
console.log(createMemberMessage(2));
console.log(createMemberMessage(999));