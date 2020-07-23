import Axios from 'axios';

export default class NodeAPI {

    getHTTPClient(port) {

        let httpClient = Axios.create({
            baseURL: `http://localhost:${port}/fileAPI`,
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

    searchFile(port, file) {
        return this.getHTTPClient(port).get(`/searchFile/${file}`);
    }

    retrieveFile(port, file) {
        return this.getHTTPClient(port).get(`/retrieveFile/${file}`);
    }

    retrieveAllFiles(port) {
        return this.getHTTPClient(port).get(`/retrieveAllFiles`);
    }
}
