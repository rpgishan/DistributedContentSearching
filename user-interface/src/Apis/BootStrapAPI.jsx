import Axios from 'axios';

export default class BootStrapAPI {

    getHTTPClient() {

        let httpClient = Axios.create({
            baseURL: "http://localhost:55556/bootstrap",
            timeout: 30000,
        });
        httpClient.defaults.headers.post['Content-Type'] = 'application/json';
        httpClient.interceptors.response.use(function (response) {
            return response;
        }, function (error) {
            console.log(error);
            return Promise.reject(error);
        });
        return httpClient;
    }

    getAllNodes() {
        return this.getHTTPClient().get("/getAllNodes");
    }
}
