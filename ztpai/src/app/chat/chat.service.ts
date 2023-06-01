import {EventEmitter, Injectable, Output} from '@angular/core';
import {HttpClient, HttpResponse} from "@angular/common/http";
import {Observable, tap} from "rxjs";
import {Urls} from "../utils/urls";
import {ChatResponse} from "./models/chat.response";
import {Message} from "@stomp/stompjs";
import {MessageWebsocketResponse} from "../message/models/message.websocket.response";
import {MessageService} from "../message/message.service";
import {RxStompService} from "../rx-stomp/rx-stomp.service";
import {AuthService} from "../auth/auth.service";
import {rxStompServiceFactory} from "../rx-stomp/rx-stomp-service-factory";

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  chats$: Array<ChatResponse> = [];
  chatWebsocketSubscriptions$: Array<any> = [];
  subscriptionForNewChats: any = undefined;
  openedChat: ChatResponse | undefined = undefined;
  you: string = this.authService.getStoredCredentials()!.username;
  @Output() newChat = new EventEmitter();
  rxStompService: RxStompService;

  constructor(private http: HttpClient, private messageService: MessageService, private authService: AuthService) {
    this.rxStompService = rxStompServiceFactory(authService);


    messageService.openedChat.subscribe(chat => this.openedChat = chat);
  }

  getYourChats(): Observable<Array<ChatResponse>> {
    return this.http.get<Array<ChatResponse>>(Urls.getYourChats);
  }

  getChat(chatId: number): Observable<ChatResponse> {
    return this.http.get<ChatResponse>(Urls.getChat(chatId));
  }

  deleteChat(chatId: number): Observable<HttpResponse<string>> {
    return this.http.delete(Urls.deleteChat(chatId), {responseType: 'text', observe: 'response'}).pipe(tap(response => {
      if (response.status === 200) {
        this.messageService.hideMessagesInChat();
        let chatIndex = this.chats$.findIndex(chat => chat.chatId === chatId);
        this.chats$.splice(chatIndex, 1);
        this.chatWebsocketSubscriptions$[chatIndex].unsubscribe();
        this.chatWebsocketSubscriptions$.splice(chatIndex, 1);

      }
    }))
  }

  fetchChats() {
    this.getYourChats().subscribe(chats => {
      this.chats$ = chats;
      this.chats$.forEach(chat => {
        this.chatWebsocketSubscriptions$.push(
          this.rxStompService
            .watch(Urls.chatDestination(chat.chatId!))
            .subscribe(this.chatWebsocketSubscriptionsCallback)
        );
      });
    });

    this.subscriptionForNewChats = this.rxStompService
      .watch(Urls.newChatsDestination(this.authService.getStoredCredentials()!.username!))
      .subscribe(this.subscriptionForNewChatsCallback);
  }

  chatWebsocketSubscriptionsCallback = (message: Message) => {
    let response: MessageWebsocketResponse = JSON.parse(message.body);
    let chat: ChatResponse = response.chatInContextOfSender.otherMemberUsername === this.you? response.chatInContextOfRecipient : response.chatInContextOfSender;
    this.chats$ = this.chats$.filter(element => element.chatId != chat.chatId);
    this.chats$.unshift(chat);
    this.newChat.emit();

    if (this.openedChat !== undefined && this.openedChat!.chatId === chat.chatId)
      this.messageService.appendNewMessage(response.message);
  }

  subscriptionForNewChatsCallback = (message: Message) => {
    let response: MessageWebsocketResponse = JSON.parse(message.body);
    let chat: ChatResponse = response.chatInContextOfSender.otherMemberUsername === this.you? response.chatInContextOfRecipient : response.chatInContextOfSender;

    this.chats$.unshift(chat);
    this.chatWebsocketSubscriptions$.unshift(
      this.rxStompService
        .watch(Urls.chatDestination(chat.chatId!))
        .subscribe(this.chatWebsocketSubscriptionsCallback)
    );
    this.newChat.emit();

    if (response.chatInContextOfSender.otherMemberUsername !== this.you)
      this.messageService.showMessagesInChat(chat);
  }

  closeAllWebsockets() {
    this.subscriptionForNewChats.unsubscribe();
    this.chatWebsocketSubscriptions$.forEach(subscription => subscription.unsubscribe());
    this.chatWebsocketSubscriptions$ = [];
  }
}
