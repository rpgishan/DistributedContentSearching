import React, {Component} from 'react';
import Parent from '../Common/ParentPage';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import Form from 'react-bootstrap/Form'
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import 'bootstrap/dist/css/bootstrap.css';
import BootStrapAPI from '../Apis/BootStrapAPI';

const styles = {
    searchArea: {
        width: '60%',
        marginLeft: '20%',
        marginRight: '20%',
        marginTop: '10%'
    },
};

class FileSearchPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            fileName: '',
            fileList: null,
            nodes: []
        };

        this.handleSearch = this.handleSearch.bind(this);
        this.handleUserInput = this.handleUserInput.bind(this);
    }

    handleSearch() {
        window.open('hi');
        // TODO: Pick a random mode from the list returned by bootstrap (node list set to state already)
        // TODO: Call the random node to initiate the file search
        // TODO: List the returned file list in a table
    }

    handleUserInput(event) {
        const target = event.target;
        const name = target.name;
        const value = target.value;
        this.setState({[name]: value});
    }

    componentDidMount() {
        new BootStrapAPI().getAllNodes().then((response) => {
            console.log(response.data.nodes);
            this.setState({nodes: response.data.nodes});
        }).catch((error) => {
            // handle errors later
        });
    }

    isSubmissionInValid() {
        const {fileName} = this.state;
        if (fileName === '') {
            return true;
        }
        return false;
    }

    renderFileSearchContent() {
        return (
            <Box style={styles.searchArea}>
                <Form onSubmit={this.handleSearch}>
                    <Row>
                        <Col xs={10}>
                            <Form.Control placeholder="File name goes here.." name="fileName" onChange={this.handleUserInput}/>
                        </Col>
                        <Col>
                            <Button variant="primary" type="submit" disabled={this.isSubmissionInValid()}>Search</Button>
                        </Col>
                    </Row>
                </Form>
            </Box>);
    }

    render() {
        return (<Parent data={this.renderFileSearchContent()}/>);
    }
}

export default (FileSearchPage);