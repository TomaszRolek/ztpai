import {RxStompConfig} from '@stomp/rx-stomp';
import {Urls} from "../utils/urls";
import {AuthService} from "../auth/auth.service";

export class myRxStompConfig extends RxStompConfig {
  constructor(private authService: AuthService) {
    super();

    this.brokerURL = Urls.websocketEndpoint;

    this.connectHeaders = {
      "access-token": authService.getStoredCredentials()!.accessToken
    };

    this.heartbeatIncoming = 0;
    this.heartbeatOutgoing = 20000;

    this.reconnectDelay = 200;

    this.debug = (msg: string): void => {
      console.log(new Date, msg);
    };

    this.beforeConnect = (stompClient: any): Promise<void> => {
      return new Promise<void>((resolve, _) => {
        resolve();
      });
    };
  }
}
