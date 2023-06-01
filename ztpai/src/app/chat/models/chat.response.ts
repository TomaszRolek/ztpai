export class ChatResponse {
  chatId: number | undefined;
  otherMemberUsername!: string;
  otherMemberAvatar!: string;
  lastMessage: string | undefined;
  lastMessageTime: string | undefined;
  messageCount: number | undefined;
}
