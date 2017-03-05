
export enum NoticeType {
    PreLogin,
    PostLogin
};

export class NoticeModel {
    constructor(
        public id: Date,
        public author: string,
        public text:string,
        public age: Number
    ){}
}